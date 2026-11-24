package com.without.without_backend.services.pedido;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.without.without_backend.entities.cliente.Cliente;
import com.without.without_backend.entities.pedido.Pedido;
import com.without.without_backend.entities.pedido.SeguimientoPedido;
import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.mappers.pedido.SeguimientoPedidoMapper;
import com.without.without_backend.repositories.cliente.ClienteRepository;
import com.without.without_backend.repositories.pedido.PedidoRepository;
import com.without.without_backend.repositories.pedido.SeguimientoPedidoRepository;
import com.without.without_backend.repositories.usuario.UsuarioRepository;
import com.without.without_backend.utils.pedido.EstadoPedido;

@ExtendWith(MockitoExtension.class)
@DisplayName("SeguimientoPedidoService: consulta y pertenencia")
class SeguimientoPedidoServiceTest {
    @Mock private SeguimientoPedidoRepository seguimientos;
    @Mock private PedidoRepository pedidos;
    @Mock private UsuarioRepository usuarios;
    @Mock private ClienteRepository clientes;
    private SeguimientoPedidoService service;

    @BeforeEach
    void preparar() {
        service = new SeguimientoPedidoService(seguimientos, pedidos,
            new SeguimientoPedidoMapper(), usuarios, clientes);
    }

    @Test
    @DisplayName("S01 - Normal: consulta historial con la consulta cronologica y convierte a DTO")
    void listarHistorialConservaDatosYOrdenRecibido() {
        when(pedidos.existsById(10)).thenReturn(true);
        when(seguimientos.findByPedidoIdPedidoOrderByFechaHoraAsc(10)).thenReturn(historial());
        var response = service.listarPorPedido(10);
        assertEquals(2, response.size());
        assertEquals(EstadoPedido.RECIBIDO, response.getFirst().estado());
        assertEquals(EstadoPedido.EN_PREPARACION, response.getLast().estado());
        assertEquals(10, response.getFirst().idPedido());
        assertEquals("Registrado", response.getFirst().observacion());
        assertEquals(historial().getFirst().getFechaHora(), response.getFirst().fechaHora());
        verify(seguimientos).findByPedidoIdPedidoOrderByFechaHoraAsc(10);
    }

    @Test
    @DisplayName("S02 - Limite: pedido existente sin historial devuelve lista vacia")
    void listarSinSeguimientosDevuelveListaVacia() {
        when(pedidos.existsById(10)).thenReturn(true);
        when(seguimientos.findByPedidoIdPedidoOrderByFechaHoraAsc(10)).thenReturn(List.of());
        assertTrue(service.listarPorPedido(10).isEmpty());
    }

    @Test
    @DisplayName("S03 - Alternativo: pedido inexistente no consulta seguimiento")
    void listarPedidoInexistenteFalla() {
        when(pedidos.existsById(999)).thenReturn(false);
        var error = assertThrows(NoSuchElementException.class, () -> service.listarPorPedido(999));
        assertEquals("Pedido no encontrado", error.getMessage());
        verifyNoInteractions(seguimientos);
    }

    @Test
    @DisplayName("S04 - Normal: cliente consulta el seguimiento de su pedido")
    void clienteConsultaPedidoPropio() {
        prepararCliente();
        when(pedidos.findByIdPedidoAndClienteIdCliente(10, 3))
            .thenReturn(Optional.of(Pedido.builder().idPedido(10).build()));
        when(seguimientos.findByPedidoIdPedidoOrderByFechaHoraAsc(10)).thenReturn(historial());
        assertEquals(2, service.listarPorPedidoCliente(10, "cliente@without.example").size());
        verify(pedidos).findByIdPedidoAndClienteIdCliente(10, 3);
    }

    @Test
    @DisplayName("S05 - Alternativo: pedido ajeno o inexistente no revela historial")
    void clienteNoPuedeConsultarPedidoAjeno() {
        prepararCliente();
        when(pedidos.findByIdPedidoAndClienteIdCliente(10, 3)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class,
            () -> service.listarPorPedidoCliente(10, "cliente@without.example"));
        verifyNoInteractions(seguimientos);
        verify(pedidos, never()).findById(any());
    }

    @Test
    @DisplayName("S06 - Alternativo: correo no registrado impide consultar")
    void usuarioInexistenteNoConsultaHistorial() {
        when(usuarios.findByCorreo("cliente@without.example")).thenReturn(Optional.empty());
        var error = assertThrows(NoSuchElementException.class,
            () -> service.listarPorPedidoCliente(10, "cliente@without.example"));
        assertEquals("Usuario no encontrado", error.getMessage());
        verifyNoInteractions(clientes, pedidos, seguimientos);
    }

    @Test
    @DisplayName("S07 - Alternativo: usuario sin cliente asociado impide consultar")
    void usuarioSinClienteNoConsultaHistorial() {
        Usuario usuario = Usuario.builder().idUsuario(2).build();
        when(usuarios.findByCorreo("cliente@without.example")).thenReturn(Optional.of(usuario));
        when(clientes.findByUsuario(usuario)).thenReturn(Optional.empty());
        var error = assertThrows(NoSuchElementException.class,
            () -> service.listarPorPedidoCliente(10, "cliente@without.example"));
        assertEquals("Cliente no encontrado", error.getMessage());
        verifyNoInteractions(pedidos, seguimientos);
    }

    private void prepararCliente() {
        Usuario usuario = Usuario.builder().idUsuario(2).build();
        when(usuarios.findByCorreo("cliente@without.example")).thenReturn(Optional.of(usuario));
        when(clientes.findByUsuario(usuario)).thenReturn(Optional.of(Cliente.builder().idCliente(3).build()));
    }
    private List<SeguimientoPedido> historial() {
        Pedido pedido = Pedido.builder().idPedido(10).build();
        LocalDateTime fecha = LocalDateTime.of(2026, 10, 1, 9, 0);
        return List.of(
            SeguimientoPedido.builder().idSeguimiento(1).pedido(pedido).estado(EstadoPedido.RECIBIDO)
                .fechaHora(fecha).observacion("Registrado").build(),
            SeguimientoPedido.builder().idSeguimiento(2).pedido(pedido).estado(EstadoPedido.EN_PREPARACION)
                .fechaHora(fecha.plusHours(1)).observacion("Preparando").build());
    }
}
