package com.without.without_backend.services.pedido;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.without.without_backend.entities.cliente.Cliente;
import com.without.without_backend.entities.pedido.Pedido;
import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.mappers.pedido.DetallePedidoMapper;
import com.without.without_backend.mappers.pedido.PedidoMapper;
import com.without.without_backend.repositories.atencion.AtencionRepository;
import com.without.without_backend.repositories.cliente.ClienteRepository;
import com.without.without_backend.repositories.pedido.*;
import com.without.without_backend.repositories.usuario.UsuarioRepository;
import com.without.without_backend.utils.pedido.EstadoPedido;
import com.without.without_backend.utils.pedido.PrioridadPedido;

@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoService: paginación de pedidos")
class PedidoPaginacionTest {
    @Mock private PedidoRepository pedidos;
    @Mock private ClienteRepository clientes;
    @Mock private ProductoRepository productos;
    @Mock private DetallePedidoRepository detalles;
    @Mock private SeguimientoPedidoRepository seguimientos;
    @Mock private UsuarioRepository usuarios;
    @Mock private AtencionRepository atenciones;
    private PedidoService service;
    private final Cliente cliente = Cliente.builder().idCliente(3).razonSocial("Clinica Norte").build();

    @BeforeEach
    void preparar() {
        service = new PedidoService(pedidos, clientes, productos, detalles, seguimientos,
            usuarios, new PedidoMapper(), new DetallePedidoMapper(), atenciones);
    }

    @Test
    @DisplayName("PG01 - Devuelve contenido y metadatos de la página solicitada")
    void devuelveMetadatosDePagina() {
        // 25 pedidos en total, se pide la página 1 (segunda) de tamaño 10
        when(pedidos.findAll(any(Pageable.class))).thenAnswer(inv -> {
            Pageable p = inv.getArgument(0);
            return new PageImpl<>(List.of(pedido(15), pedido(14)), p, 25);
        });

        var pagina = service.listarPaginado(1, 10, null);

        assertAll(
            () -> assertEquals(2, pagina.contenido().size()),
            () -> assertEquals(15, pagina.contenido().get(0).idPedido()),
            () -> assertEquals("Clinica Norte", pagina.contenido().get(0).razonSocial()),
            () -> assertEquals(1, pagina.pagina()),
            () -> assertEquals(10, pagina.tamanio()),
            () -> assertEquals(25, pagina.totalElementos()),
            () -> assertEquals(3, pagina.totalPaginas()),
            () -> assertFalse(pagina.primera()),
            () -> assertFalse(pagina.ultima())
        );
    }

    @Test
    @DisplayName("PG02 - Ordena por idPedido descendente (más recientes primero)")
    void ordenaPorIdDescendente() {
        when(pedidos.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        service.listarPaginado(0, 5, null);

        var captor = ArgumentCaptor.forClass(Pageable.class);
        verify(pedidos).findAll(captor.capture());
        assertEquals(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "idPedido")), captor.getValue());
    }

    @Test
    @DisplayName("PG03 - Con estado filtra en base de datos, no en memoria")
    void filtraPorEstado() {
        when(pedidos.findByEstado(eq(EstadoPedido.RECIBIDO), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(pedido(7))));

        var pagina = service.listarPaginado(0, 10, EstadoPedido.RECIBIDO);

        assertEquals(1, pagina.totalElementos());
        verify(pedidos, never()).findAll(any(Pageable.class));
    }

    @ParameterizedTest(name = "pagina={0}, tamanio={1}")
    @CsvSource({"-1, 10", "0, 0", "0, 51", "0, -5"})
    @DisplayName("PG04 - Rechaza página negativa o tamaño fuera de 1..50")
    void rechazaParametrosInvalidos(int pagina, int tamanio) {
        assertThrows(IllegalArgumentException.class, () -> service.listarPaginado(pagina, tamanio, null));
        verifyNoInteractions(pedidos);
    }

    @Test
    @DisplayName("PG05 - Cliente solo pagina sus propios pedidos")
    void clienteSoloVeSusPedidos() {
        Usuario usuario = Usuario.builder().idUsuario(9).correo("cliente@without.example").build();
        when(usuarios.findByCorreo("cliente@without.example")).thenReturn(Optional.of(usuario));
        when(clientes.findByUsuario(usuario)).thenReturn(Optional.of(cliente));
        when(pedidos.findByClienteIdCliente(eq(3), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(pedido(1))));

        var pagina = service.listarMisPedidosPaginado("cliente@without.example", 0, 10, null);

        assertEquals(3, pagina.contenido().get(0).idCliente());
        verify(pedidos, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("PG06 - Usuario sin cliente asociado recibe 404")
    void usuarioSinClienteFalla() {
        when(usuarios.findByCorreo("x@without.example")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> service.listarMisPedidosPaginado("x@without.example", 0, 10, null));
    }

    private Pedido pedido(int id) {
        return Pedido.builder().idPedido(id).estado(EstadoPedido.RECIBIDO)
            .prioridad(PrioridadPedido.values()[0]).fechaRegistro(LocalDate.now()).cliente(cliente).build();
    }
}
