package com.without.without_backend.services.pedido;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.without.without_backend.dto.pedido.detallepedido.DetallePedidoRequest;
import com.without.without_backend.dto.pedido.pedido.PedidoRequest;
import com.without.without_backend.dto.pedido.seguimientopedido.CambioEstadoPedidoRequest;
import com.without.without_backend.dto.pedido.seguimientopedido.CancelarPedidoRequest;
import com.without.without_backend.entities.cliente.Cliente;
import com.without.without_backend.entities.pedido.*;
import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.mappers.pedido.DetallePedidoMapper;
import com.without.without_backend.mappers.pedido.PedidoMapper;
import com.without.without_backend.repositories.cliente.ClienteRepository;
import com.without.without_backend.repositories.pedido.*;
import com.without.without_backend.repositories.usuario.UsuarioRepository;
import com.without.without_backend.utils.Estado;
import com.without.without_backend.utils.pedido.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoService: registro, estados y cancelacion")
class PedidoServiceTest {
    @Mock private PedidoRepository pedidos;
    @Mock private ClienteRepository clientes;
    @Mock private ProductoRepository productos;
    @Mock private DetallePedidoRepository detalles;
    @Mock private SeguimientoPedidoRepository seguimientos;
    @Mock private UsuarioRepository usuarios;
    @Mock private com.without.without_backend.repositories.atencion.AtencionRepository atenciones;
    private PedidoService service;

    @BeforeEach
    void preparar() {
        service = new PedidoService(pedidos, clientes, productos, detalles, seguimientos,
            usuarios, new PedidoMapper(), new DetallePedidoMapper(), atenciones);
    }

    @Test
    @DisplayName("PD01 - Normal: registro crea pedido, detalle con precio vigente y seguimiento inicial")
    void insertarPedidoCreaDetalleYSeguimiento() {
        Cliente cliente = prepararCliente();
        Producto producto = Producto.builder().idProducto(4).nombre("Oxigeno")
            .estado(Estado.ACTIVO).precioUnitario(new BigDecimal("8.50")).build();
        when(productos.findById(4)).thenReturn(Optional.of(producto));
        when(pedidos.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            pedido.setIdPedido(10);
            return pedido;
        });
        LocalDateTime antes = LocalDateTime.now();

        var response = service.insertar(solicitud(List.of(detalle(4))), "cliente@without.example");

        var pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
        var detalleCaptor = ArgumentCaptor.forClass(DetallePedido.class);
        var seguimientoCaptor = ArgumentCaptor.forClass(SeguimientoPedido.class);
        verify(pedidos).save(pedidoCaptor.capture());
        verify(detalles).save(detalleCaptor.capture());
        verify(seguimientos).save(seguimientoCaptor.capture());
        assertAll(
            () -> assertEquals(EstadoPedido.RECIBIDO, response.estado()),
            () -> assertEquals(10, response.idPedido()),
            () -> assertEquals(3, response.idCliente()),
            () -> assertSame(cliente, pedidoCaptor.getValue().getCliente()),
            () -> assertEquals(new BigDecimal("8.50"), detalleCaptor.getValue().getPrecioUnitario()),
            () -> assertEquals(new BigDecimal("2.00"), detalleCaptor.getValue().getCantidad()),
            () -> assertEquals(Estado.ACTIVO, detalleCaptor.getValue().getEstado()),
            () -> assertSame(producto, detalleCaptor.getValue().getProducto()),
            () -> assertSame(pedidoCaptor.getValue(), detalleCaptor.getValue().getPedido()),
            () -> assertSame(pedidoCaptor.getValue(), seguimientoCaptor.getValue().getPedido()),
            () -> assertEquals(EstadoPedido.RECIBIDO, seguimientoCaptor.getValue().getEstado()),
            () -> assertFalse(seguimientoCaptor.getValue().getFechaHora().isBefore(antes)),
            () -> assertFalse(seguimientoCaptor.getValue().getFechaHora().isAfter(LocalDateTime.now()))
        );
    }

    @Test
    @DisplayName("PD02 - Alternativo: productos repetidos se rechazan antes de guardar")
    void insertarConProductosRepetidosFalla() {
        prepararCliente();
        var error = assertThrows(IllegalArgumentException.class,
            () -> service.insertar(solicitud(List.of(detalle(4), detalle(4))), "cliente@without.example"));
        assertEquals("Un producto no puede repetirse dentro del mismo pedido", error.getMessage());
        verifyNoInteractions(pedidos, productos, detalles, seguimientos);
    }

    @Test
    @DisplayName("PD03 - Alternativo: producto inactivo impide guardar detalle y seguimiento")
    void insertarConProductoInactivoFalla() {
        prepararCliente();
        when(pedidos.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productos.findById(4)).thenReturn(Optional.of(Producto.builder()
            .idProducto(4).nombre("Oxigeno").estado(Estado.INACTIVO).build()));
        var error = assertThrows(IllegalArgumentException.class,
            () -> service.insertar(solicitud(List.of(detalle(4))), "cliente@without.example"));
        assertEquals("El producto Oxigeno no está activo", error.getMessage());
        verifyNoInteractions(detalles, seguimientos);
        // El servicio guarda el pedido antes de validar productos. El rollback real requiere una prueba de integracion.
    }

    @ParameterizedTest(name = "PD04 - Normal: {0} -> {1}")
    @CsvSource({"RECIBIDO,EN_PREPARACION", "EN_PREPARACION,DESPACHADO", "DESPACHADO,ENTREGADO"})
    void avanzarAlSiguienteEstadoRegistraSeguimiento(EstadoPedido actual, EstadoPedido nuevo) {
        Pedido pedido = pedido(actual);
        when(pedidos.findById(10)).thenReturn(Optional.of(pedido));

        var response = service.cambiarEstado(10, new CambioEstadoPedidoRequest(nuevo, "Avance autorizado"));

        var captor = ArgumentCaptor.forClass(SeguimientoPedido.class);
        verify(seguimientos).save(captor.capture());
        assertEquals(nuevo, response.estado());
        assertEquals(nuevo, pedido.getEstado());
        assertEquals(nuevo, captor.getValue().getEstado());
        assertEquals("Avance autorizado", captor.getValue().getObservacion());
        assertSame(pedido, captor.getValue().getPedido());
    }

    @ParameterizedTest(name = "PD05 - Rechazo: {0} -> {1}")
    @CsvSource({"RECIBIDO,ENTREGADO", "DESPACHADO,EN_PREPARACION", "RECIBIDO,RECIBIDO",
        "RECIBIDO,CANCELADO", "ENTREGADO,EN_PREPARACION", "CANCELADO,RECIBIDO"})
    void rechazaCambioInvalidoSinModificar(EstadoPedido actual, EstadoPedido nuevo) {
        Pedido pedido = pedido(actual);
        when(pedidos.findById(10)).thenReturn(Optional.of(pedido));
        assertThrows(IllegalArgumentException.class,
            () -> service.cambiarEstado(10, new CambioEstadoPedidoRequest(nuevo, null)));
        assertEquals(actual, pedido.getEstado());
        verifyNoInteractions(seguimientos);
        verify(pedidos, never()).save(any());
    }

    @Test
    @DisplayName("PD06 - Normal: cancelar registra estado y motivo")
    void cancelarPedidoRegistraMotivo() {
        Pedido pedido = pedido(EstadoPedido.RECIBIDO);
        when(pedidos.findById(10)).thenReturn(Optional.of(pedido));
        var response = service.cancelar(10, new CancelarPedidoRequest("Cliente desistio"));
        var captor = ArgumentCaptor.forClass(SeguimientoPedido.class);
        verify(seguimientos).save(captor.capture());
        assertEquals(EstadoPedido.CANCELADO, response.estado());
        assertEquals(EstadoPedido.CANCELADO, captor.getValue().getEstado());
        assertEquals("Cliente desistio", captor.getValue().getObservacion());
        assertSame(pedido, captor.getValue().getPedido());
    }

    @ParameterizedTest(name = "PD07 - Limite: cancelar pedido {0}")
    @EnumSource(value = EstadoPedido.class, names = {"DESPACHADO", "ENTREGADO", "CANCELADO"})
    void cancelarEstadoTerminalFalla(EstadoPedido estado) {
        Pedido pedido = pedido(estado);
        when(pedidos.findById(10)).thenReturn(Optional.of(pedido));
        assertThrows(IllegalArgumentException.class, () -> service.cancelar(10, new CancelarPedidoRequest(null)));
        assertEquals(estado, pedido.getEstado());
        verifyNoInteractions(seguimientos);
    }

    @Test
    @DisplayName("PD08 - Alternativo: cambiar estado de ID inexistente no registra historial")
    void cambiarPedidoInexistenteFalla() {
        when(pedidos.findById(999)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class,
            () -> service.cambiarEstado(999, new CambioEstadoPedidoRequest(EstadoPedido.EN_PREPARACION, null)));
        verifyNoInteractions(seguimientos);
    }

    @Test
    @DisplayName("PD09 - Alternativo: cliente no puede cancelar un pedido ajeno")
    void cancelarPedidoAjenoFalla() {
        prepararCliente();
        when(pedidos.findByIdPedidoAndClienteIdCliente(10, 3)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class,
            () -> service.cancelarMiPedido(10, new CancelarPedidoRequest("Cancelar"), "cliente@without.example"));
        verifyNoInteractions(seguimientos);
        verify(pedidos, never()).findById(any());
    }

    @ParameterizedTest
    @EnumSource(value = EstadoPedido.class, names = {"DESPACHADO", "ENTREGADO", "CANCELADO"})
    void clienteNoPuedeCancelarPedidoDespachadoOTerminal(EstadoPedido estado) {
        prepararCliente();
        Pedido pedido = pedido(estado);
        when(pedidos.findByIdPedidoAndClienteIdCliente(10, 3)).thenReturn(Optional.of(pedido));
        assertThrows(IllegalArgumentException.class, () -> service.cancelarMiPedido(10,
            new CancelarPedidoRequest("Cancelar"), "cliente@without.example"));
        assertEquals(estado, pedido.getEstado());
        verifyNoInteractions(seguimientos);
    }

    @Test void cancelarEnPreparacionCancelaVisitaYConservaMotivo() {
        Pedido pedido = pedido(EstadoPedido.EN_PREPARACION);
        var visita = com.without.without_backend.entities.atencion.Atencion.builder().pedido(pedido)
            .estado(com.without.without_backend.utils.atencion.EstadoAtencion.PROGRAMADA).build();
        when(pedidos.findById(10)).thenReturn(Optional.of(pedido));
        when(atenciones.findByPedidoIdPedido(10)).thenReturn(List.of(visita));
        service.cancelar(10, new CancelarPedidoRequest("Cambio de necesidad"));
        assertEquals(EstadoPedido.CANCELADO, pedido.getEstado());
        assertEquals(com.without.without_backend.utils.atencion.EstadoAtencion.CANCELADA, visita.getEstado());
        assertEquals("Cambio de necesidad", visita.getObservaciones());
        verify(atenciones).save(visita);
        verify(seguimientos).save(argThat(s -> s.getEstado() == EstadoPedido.CANCELADO && "Cambio de necesidad".equals(s.getObservacion())));
    }
    @Test void clienteCancelaSuPedidoYLaVisitaProgramada() {
        prepararCliente();
        Pedido pedido = pedido(EstadoPedido.EN_PREPARACION);
        var visita = com.without.without_backend.entities.atencion.Atencion.builder().pedido(pedido)
            .estado(com.without.without_backend.utils.atencion.EstadoAtencion.PROGRAMADA).build();
        when(pedidos.findByIdPedidoAndClienteIdCliente(10, 3)).thenReturn(Optional.of(pedido));
        when(atenciones.findByPedidoIdPedido(10)).thenReturn(List.of(visita));
        service.cancelarMiPedido(10, new CancelarPedidoRequest("Ya no requerido"), "cliente@without.example");
        assertEquals(EstadoPedido.CANCELADO, pedido.getEstado());
        assertEquals(com.without.without_backend.utils.atencion.EstadoAtencion.CANCELADA, visita.getEstado());
        verify(atenciones).save(visita);
    }
    @Test void noCancelaSiExisteVisitaEnCursoAunquePedidoEsteDesactualizado() {
        Pedido pedido = pedido(EstadoPedido.EN_PREPARACION);
        var visita = com.without.without_backend.entities.atencion.Atencion.builder().pedido(pedido)
            .estado(com.without.without_backend.utils.atencion.EstadoAtencion.EN_CURSO).build();
        when(pedidos.findById(10)).thenReturn(Optional.of(pedido));
        when(atenciones.findByPedidoIdPedido(10)).thenReturn(List.of(visita));
        assertThrows(IllegalArgumentException.class, () -> service.cancelar(10, new CancelarPedidoRequest("Cancelar")));
        assertEquals(EstadoPedido.EN_PREPARACION, pedido.getEstado());
        verify(atenciones, never()).save(any());
        verifyNoInteractions(seguimientos);
    }

    private Cliente prepararCliente() {
        Usuario usuario = Usuario.builder().idUsuario(2).build();
        Cliente cliente = Cliente.builder().idCliente(3).usuario(usuario).build();
        when(usuarios.findByCorreo("cliente@without.example")).thenReturn(Optional.of(usuario));
        when(clientes.findByUsuario(usuario)).thenReturn(Optional.of(cliente));
        return cliente;
    }
    private Pedido pedido(EstadoPedido estado) {
        return Pedido.builder().idPedido(10).estado(estado)
            .cliente(Cliente.builder().idCliente(3).build()).build();
    }
    private DetallePedidoRequest detalle(int id) {
        return new DetallePedidoRequest(new BigDecimal("2.00"), id);
    }
    private PedidoRequest solicitud(List<DetallePedidoRequest> detalles) {
        return new PedidoRequest(LocalDate.of(2026, 10, 10), PrioridadPedido.MEDIA, detalles);
    }
}
