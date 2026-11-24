package com.without.without_backend.services.pedido;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.without.without_backend.dto.pedido.detallepedido.*;
import com.without.without_backend.entities.cliente.Cliente;
import com.without.without_backend.entities.pedido.*;
import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.mappers.pedido.DetallePedidoMapper;
import com.without.without_backend.repositories.cliente.ClienteRepository;
import com.without.without_backend.repositories.pedido.*;
import com.without.without_backend.repositories.usuario.UsuarioRepository;
import com.without.without_backend.utils.Estado;
import com.without.without_backend.utils.pedido.EstadoPedido;

/** Unidad pura: repositorios simulados; no inicia Spring ni abre conexiones. */
@ExtendWith(MockitoExtension.class)
class DetallePedidoServiceTest {
    private static final String CORREO = "cliente@example.test";
    @Mock DetallePedidoRepository detalles;
    @Mock PedidoRepository pedidos;
    @Mock ProductoRepository productos;
    @Mock UsuarioRepository usuarios;
    @Mock ClienteRepository clientes;
    DetallePedidoService service;
    Pedido pedido;
    Producto producto;

    @BeforeEach void preparar() {
        service = new DetallePedidoService(detalles, pedidos, productos,
            new DetallePedidoMapper(), usuarios, clientes);
        pedido = Pedido.builder().idPedido(10).estado(EstadoPedido.RECIBIDO).build();
        producto = Producto.builder().idProducto(4).nombre("Oxigeno")
            .precioUnitario(new BigDecimal("8.50")).estado(Estado.ACTIVO).build();
    }

    private void propietario(boolean pertenece) {
        Usuario usuario = Usuario.builder().correo(CORREO).build();
        Cliente cliente = Cliente.builder().idCliente(3).usuario(usuario).build();
        when(usuarios.findByCorreo(CORREO)).thenReturn(Optional.of(usuario));
        when(clientes.findByUsuario(usuario)).thenReturn(Optional.of(cliente));
        when(pedidos.findByIdPedidoAndClienteIdCliente(10, 3))
            .thenReturn(pertenece ? Optional.of(pedido) : Optional.empty());
    }

    private DetallePedido detalle(Pedido origen, Estado estado) {
        return DetallePedido.builder().idDetalle(20).pedido(origen).producto(producto)
            .cantidad(new BigDecimal("2.00")).precioUnitario(new BigDecimal("7.00"))
            .estado(estado).build();
    }

    @Test void soloListaDetallesActivosDelPedidoPropio() {
        propietario(true);
        when(detalles.findByPedidoIdPedidoAndEstado(10, Estado.ACTIVO))
            .thenReturn(List.of(detalle(pedido, Estado.ACTIVO)));
        var result = service.listarActivosPorPedidoCliente(10, CORREO);
        assertEquals(1, result.size());
        assertEquals(20, result.getFirst().idDetalle());
        assertEquals(Estado.ACTIVO, result.getFirst().estado());
        verify(detalles, never()).findByPedidoIdPedido(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"listar", "agregar", "cantidad", "cancelar"})
    void pedidoAjenoSeRechazaAntesDeLeerOModificarDetalles(String operacion) {
        propietario(false);
        assertThrows(NoSuchElementException.class, () -> {
            switch (operacion) {
                case "listar" -> service.listarActivosPorPedidoCliente(10, CORREO);
                case "agregar" -> service.agregarDetalleCliente(10,
                    new DetallePedidoRequest(BigDecimal.ONE, 4), CORREO);
                case "cantidad" -> service.agregarCantidadCliente(10, 20,
                    new AgregarCantidadDetalleRequest(BigDecimal.ONE), CORREO);
                case "cancelar" -> service.cancelarCliente(10, 20, CORREO);
                default -> throw new AssertionError(operacion);
            }
        });
        verifyNoInteractions(detalles, productos);
        verify(pedidos, never()).save(any());
    }

    @Test void usuarioDesconocidoNoConsultaPedidos() {
        when(usuarios.findByCorreo(CORREO)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class,
            () -> service.listarActivosPorPedidoCliente(10, CORREO));
        verifyNoInteractions(clientes, pedidos, detalles);
    }

    @Test void usuarioSinClienteNoConsultaPedidos() {
        Usuario usuario = Usuario.builder().correo(CORREO).build();
        when(usuarios.findByCorreo(CORREO)).thenReturn(Optional.of(usuario));
        when(clientes.findByUsuario(usuario)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class,
            () -> service.listarActivosPorPedidoCliente(10, CORREO));
        verifyNoInteractions(pedidos, detalles);
    }

    @ParameterizedTest
    @EnumSource(value = EstadoPedido.class, names = "RECIBIDO", mode = EnumSource.Mode.EXCLUDE)
    void pedidoNoEditableRechazaAgregarAumentarYCancelar(EstadoPedido estado) {
        pedido.setEstado(estado);
        when(pedidos.findById(10)).thenReturn(Optional.of(pedido));
        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> service.agregarDetalle(10,
                new DetallePedidoRequest(BigDecimal.ONE, 4))),
            () -> assertThrows(IllegalArgumentException.class, () -> service.agregarCantidad(10, 20,
                new AgregarCantidadDetalleRequest(BigDecimal.ONE))),
            () -> assertThrows(IllegalArgumentException.class, () -> service.cancelar(10, 20))
        );
        verifyNoInteractions(detalles, productos);
    }

    @Test void productoInactivoNoSeAgrega() {
        when(pedidos.findById(10)).thenReturn(Optional.of(pedido));
        producto.setEstado(Estado.INACTIVO);
        when(productos.findById(4)).thenReturn(Optional.of(producto));
        assertThrows(IllegalArgumentException.class, () -> service.agregarDetalle(10,
            new DetallePedidoRequest(BigDecimal.ONE, 4)));
        verifyNoInteractions(detalles);
    }

    @Test void productoDuplicadoNoSeGuarda() {
        when(pedidos.findById(10)).thenReturn(Optional.of(pedido));
        when(productos.findById(4)).thenReturn(Optional.of(producto));
        when(detalles.existsByPedidoIdPedidoAndProductoIdProductoAndEstado(10, 4, Estado.ACTIVO))
            .thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.agregarDetalle(10,
            new DetallePedidoRequest(BigDecimal.ONE, 4)));
        verify(detalles, never()).save(any());
    }

    @Test void agregarDetallePropioDebeGuardarEstadoActivoYPrecioVigente() {
        propietario(true);
        when(productos.findById(4)).thenReturn(Optional.of(producto));
        when(detalles.save(any(DetallePedido.class))).thenAnswer(inv -> inv.getArgument(0));
        var result = service.agregarDetalleCliente(10,
            new DetallePedidoRequest(new BigDecimal("1.25"), 4), CORREO);
        var captor = org.mockito.ArgumentCaptor.forClass(DetallePedido.class);
        verify(detalles).save(captor.capture());
        assertAll(
            () -> assertSame(pedido, captor.getValue().getPedido()),
            () -> assertSame(producto, captor.getValue().getProducto()),
            () -> assertEquals(new BigDecimal("1.25"), result.cantidad()),
            () -> assertEquals(new BigDecimal("8.50"), result.precioUnitario()),
            () -> assertEquals(Estado.ACTIVO, captor.getValue().getEstado(),
                "Un detalle nuevo debe ser activo y cumplir la columna NOT NULL")
        );
    }

    @Test void aumentaCantidadSinCambiarElPrecioHistorico() {
        propietario(true);
        DetallePedido detalle = detalle(pedido, Estado.ACTIVO);
        when(detalles.findById(20)).thenReturn(Optional.of(detalle));
        when(detalles.save(detalle)).thenReturn(detalle);
        var result = service.agregarCantidadCliente(10, 20,
            new AgregarCantidadDetalleRequest(new BigDecimal("0.01")), CORREO);
        assertEquals(new BigDecimal("2.01"), result.cantidad());
        assertEquals(new BigDecimal("7.00"), result.precioUnitario());
        verifyNoInteractions(productos);
    }

    @Test void cancelarEsBajaLogicaYConservaCantidad() {
        propietario(true);
        DetallePedido detalle = detalle(pedido, Estado.ACTIVO);
        when(detalles.findById(20)).thenReturn(Optional.of(detalle));
        when(detalles.countByPedidoIdPedidoAndEstado(10, Estado.ACTIVO)).thenReturn(2L);
        when(detalles.save(detalle)).thenReturn(detalle);
        var result = service.cancelarCliente(10, 20, CORREO);
        assertEquals(Estado.INACTIVO, result.estado());
        assertEquals(new BigDecimal("2.00"), result.cantidad());
        verify(detalles, never()).delete(any());
        verify(pedidos, never()).save(any());
    }

    @Test void noSePuedeQuitarElUltimoProductoDelPedido() {
        propietario(true);
        when(detalles.findById(20)).thenReturn(Optional.of(detalle(pedido, Estado.ACTIVO)));
        when(detalles.countByPedidoIdPedidoAndEstado(10, Estado.ACTIVO)).thenReturn(1L);
        var error = assertThrows(IllegalArgumentException.class, () -> service.cancelarCliente(10, 20, CORREO));
        assertTrue(error.getMessage().contains("al menos un producto"));
        verify(detalles, never()).save(any());
    }

    @Test void detalleDeOtroPedidoNoSeModifica() {
        when(pedidos.findById(10)).thenReturn(Optional.of(pedido));
        Pedido otro = Pedido.builder().idPedido(99).build();
        when(detalles.findById(20)).thenReturn(Optional.of(detalle(otro, Estado.ACTIVO)));
        assertThrows(IllegalArgumentException.class, () -> service.cancelar(10, 20));
        verify(detalles, never()).save(any());
    }

    @Test void detalleInactivoNoSeAumentaNiSeCancelaDeNuevo() {
        when(pedidos.findById(10)).thenReturn(Optional.of(pedido));
        when(detalles.findById(20)).thenReturn(Optional.of(detalle(pedido, Estado.INACTIVO)));
        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> service.agregarCantidad(10, 20,
                new AgregarCantidadDetalleRequest(BigDecimal.ONE))),
            () -> assertThrows(IllegalArgumentException.class, () -> service.cancelar(10, 20))
        );
        verify(detalles, never()).save(any());
    }

    @Test void detalleInexistenteNoSeCreaAlEditar() {
        when(pedidos.findById(10)).thenReturn(Optional.of(pedido));
        when(detalles.findById(20)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.agregarCantidad(10, 20,
            new AgregarCantidadDetalleRequest(BigDecimal.ONE)));
        verify(detalles, never()).save(any());
    }
}
