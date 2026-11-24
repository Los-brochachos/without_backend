package com.without.without_backend.services.atencion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import com.without.without_backend.entities.atencion.Atencion;
import com.without.without_backend.mappers.atencion.AtencionMapper;
import com.without.without_backend.repositories.atencion.AtencionRepository;
import com.without.without_backend.utils.atencion.EstadoAtencion;

class AtencionConductorTest {
    private final AtencionRepository repository = mock(AtencionRepository.class);
    private final com.without.without_backend.repositories.pedido.SeguimientoPedidoRepository seguimientos = mock(com.without.without_backend.repositories.pedido.SeguimientoPedidoRepository.class);
    private final com.without.without_backend.repositories.trabajador.ConductorRepository conductores = mock(com.without.without_backend.repositories.trabajador.ConductorRepository.class);
    @org.junit.jupiter.api.BeforeEach void conductorAutenticado() {
        when(conductores.bloquearPorCorreo(correo)).thenReturn(Optional.of(com.without.without_backend.entities.trabajador.Conductor.builder().idTrabajador(4).build()));
    }
    private final AtencionService service = new AtencionService(repository, mock(com.without.without_backend.repositories.pedido.PedidoRepository.class), conductores, null, null, new AtencionMapper(), seguimientos);
    private final String correo = "conductor@ejemplo.com";
    private Atencion preparar(EstadoAtencion estado) {
        var pedido = com.without.without_backend.entities.pedido.Pedido.builder().idPedido(1)
            .estado(estado == EstadoAtencion.PROGRAMADA ? com.without.without_backend.utils.pedido.EstadoPedido.EN_PREPARACION : com.without.without_backend.utils.pedido.EstadoPedido.DESPACHADO).build();
        var a = Atencion.builder().idAtencion(8).pedido(pedido).estado(estado).build();
        when(repository.findByIdAtencionAndConductor_Trabajador_Usuario_Correo(8, correo)).thenReturn(Optional.of(a));
        when(repository.save(any(Atencion.class))).thenAnswer(i -> i.getArgument(0));
        return a;
    }
    @Test void listaUnicamenteLasAsignadasAlCorreo() {
        when(repository.findByConductor_Trabajador_Usuario_Correo(correo)).thenReturn(List.of());
        assertTrue(service.listarAsignadas(correo).isEmpty());
        verify(repository).findByConductor_Trabajador_Usuario_Correo(correo);
        verify(repository, never()).findAll();
    }
    @Test void noPuedeIniciarAtencionAjena() {
        assertThrows(NoSuchElementException.class, () -> service.iniciar(8, correo));
        verify(repository, never()).save(any());
    }
    @Test void noPuedeFinalizarAtencionAjena() {
        assertThrows(NoSuchElementException.class, () -> service.finalizar(8, correo));
        verify(repository, never()).save(any());
    }
    @Test void iniciarRegistraEstadoYHora() {
        var a = preparar(EstadoAtencion.PROGRAMADA);
        var resultado = service.iniciar(8, correo);
        assertEquals(EstadoAtencion.EN_CURSO, resultado.estado());
        assertNotNull(resultado.fechaInicio());
        assertNull(resultado.fechaFin());
        assertEquals(com.without.without_backend.utils.pedido.EstadoPedido.DESPACHADO, a.getPedido().getEstado());
        verify(seguimientos).save(argThat(s -> s.getPedido() == a.getPedido() && s.getEstado() == a.getPedido().getEstado()));
    }
    @Test void finalizarRegistraEstadoYConservaInicio() {
        var a = preparar(EstadoAtencion.EN_CURSO);
        var inicio = java.time.LocalDateTime.now().minusMinutes(10); a.setFechaInicio(inicio);
        var resultado = service.finalizar(8, correo);
        assertEquals(EstadoAtencion.FINALIZADA, resultado.estado());
        assertEquals(inicio, resultado.fechaInicio());
        assertTrue(resultado.fechaFin().isAfter(inicio));
        assertEquals(com.without.without_backend.utils.pedido.EstadoPedido.ENTREGADO, a.getPedido().getEstado());
        verify(seguimientos).save(argThat(s -> s.getPedido() == a.getPedido() && s.getEstado() == a.getPedido().getEstado()));
    }
    @Test void noAceptaOtraMientrasTieneUnRecorridoEnCurso() {
        var a = preparar(EstadoAtencion.PROGRAMADA);
        when(repository.existsByConductor_IdTrabajadorAndEstado(4, EstadoAtencion.EN_CURSO)).thenReturn(true);
        var error = assertThrows(IllegalArgumentException.class, () -> service.iniciar(8, correo));
        assertTrue(error.getMessage().contains("EN_CURSO"));
        assertEquals(EstadoAtencion.PROGRAMADA, a.getEstado());
        assertNull(a.getFechaInicio());
        verify(repository, never()).save(any());
        verifyNoInteractions(seguimientos);
    }
    @Test void puedeAceptarOtraCuandoYaNoTieneRecorridoEnCurso() {
        preparar(EstadoAtencion.PROGRAMADA);
        when(repository.existsByConductor_IdTrabajadorAndEstado(4, EstadoAtencion.EN_CURSO)).thenReturn(false);
        assertEquals(EstadoAtencion.EN_CURSO, service.iniciar(8, correo).estado());
        verify(conductores).bloquearPorCorreo(correo);
    }
    @Test void noIniciaPedidoCancelado() {
        var a = preparar(EstadoAtencion.PROGRAMADA);
        a.getPedido().setEstado(com.without.without_backend.utils.pedido.EstadoPedido.CANCELADO);
        assertThrows(IllegalArgumentException.class, () -> service.iniciar(8, correo));
        verify(repository, never()).save(any());
        verifyNoInteractions(seguimientos);
    }
    @Test void noFinalizaSinIniciar() {
        preparar(EstadoAtencion.PROGRAMADA);
        assertThrows(IllegalArgumentException.class, () -> service.finalizar(8, correo));
        verify(repository, never()).save(any());
    }
    @Test void noIniciaDosVeces() {
        preparar(EstadoAtencion.EN_CURSO);
        assertThrows(IllegalArgumentException.class, () -> service.iniciar(8, correo));
        verify(repository, never()).save(any());
    }
    @Test void noFinalizaDosVeces() {
        preparar(EstadoAtencion.FINALIZADA);
        assertThrows(IllegalArgumentException.class, () -> service.finalizar(8, correo));
        verify(repository, never()).save(any());
    }
}
