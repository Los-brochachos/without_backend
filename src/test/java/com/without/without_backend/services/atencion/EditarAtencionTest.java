package com.without.without_backend.services.atencion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.without.without_backend.dto.atencion.atencion.EditarAtencionRequest;
import com.without.without_backend.entities.atencion.Atencion;
import com.without.without_backend.entities.cisterna.Cisterna;
import com.without.without_backend.entities.pedido.Pedido;
import com.without.without_backend.entities.trabajador.Conductor;
import com.without.without_backend.entities.trabajador.Trabajador;
import com.without.without_backend.mappers.atencion.AtencionMapper;
import com.without.without_backend.repositories.atencion.AtencionRepository;
import com.without.without_backend.repositories.cisterna.CisternaRepository;
import com.without.without_backend.repositories.trabajador.ConductorRepository;
import com.without.without_backend.utils.Estado;
import com.without.without_backend.utils.atencion.EstadoAtencion;

class EditarAtencionTest {
    private final AtencionRepository repository = mock(AtencionRepository.class);
    private final ConductorRepository conductores = mock(ConductorRepository.class);
    private final CisternaRepository cisternas = mock(CisternaRepository.class);
    private final AtencionService service = new AtencionService(repository, null, conductores, cisternas, null, new AtencionMapper(), null);
    private final LocalDateTime inicio = LocalDateTime.of(2031, 1, 1, 10, 0);
    private Atencion atencion;
    private Cisterna cisterna;
    private EditarAtencionRequest request(LocalDateTime fin) { return new EditarAtencionRequest(2, 3, inicio, fin, "Cambio de horario"); }
    @BeforeEach void preparar() {
        atencion = Atencion.builder().idAtencion(8).pedido(Pedido.builder().idPedido(11).build()).estado(EstadoAtencion.PROGRAMADA).fechaInicioProgramada(inicio).fechaFinProgramada(inicio.plusHours(1)).build();
        when(repository.findById(8)).thenReturn(Optional.of(atencion));
        var trabajador = mock(Trabajador.class); when(trabajador.getEstado()).thenReturn(Estado.ACTIVO);
        var conductor = Conductor.builder().idTrabajador(2).trabajador(trabajador).build();
        when(conductores.findById(2)).thenReturn(Optional.of(conductor));
        cisterna = Cisterna.builder().idCisterna(3).estado(Estado.ACTIVO).build();
        when(cisternas.findById(3)).thenReturn(Optional.of(cisterna));
        when(repository.save(any(Atencion.class))).thenAnswer(i -> i.getArgument(0));
    }
    @Test void guardaCambiosSinModificarPedidoNiEstado() {
        var resultado = service.editar(8, request(inicio.plusHours(2)));
        assertEquals(8, resultado.idAtencion()); assertEquals(11, resultado.idPedido());
        assertEquals(EstadoAtencion.PROGRAMADA, resultado.estado());
        assertEquals(2, resultado.idConductor()); assertEquals(3, resultado.idCisterna());
        assertEquals(inicio.plusHours(2), resultado.fechaFinProgramada());
        assertEquals("Cambio de horario", resultado.observaciones());
    }
    @Test void laPropiaAtencionNoBloqueaSusRecursos() {
        when(repository.findByCisterna_IdCisterna(3)).thenReturn(List.of(atencion));
        when(repository.findByConductor_IdTrabajador(2)).thenReturn(List.of(atencion));
        assertDoesNotThrow(() -> service.editar(8, request(inicio.plusHours(1))));
    }
    @Test void bloqueaEdicionDeEstadosQueYaNoSonProgramados() {
        for (var estado : List.of(EstadoAtencion.EN_CURSO, EstadoAtencion.FINALIZADA, EstadoAtencion.CANCELADA)) {
            atencion.setEstado(estado);
            assertThrows(IllegalArgumentException.class, () -> service.editar(8, request(inicio.plusHours(1))));
        }
        verify(repository, never()).save(any());
    }
    @Test void rechazaFinIgualAlInicio() {
        assertThrows(IllegalArgumentException.class, () -> service.editar(8, request(inicio)));
        verify(repository, never()).save(any());
    }
    private Atencion otra() { return Atencion.builder().idAtencion(9).estado(EstadoAtencion.PROGRAMADA).fechaInicioProgramada(inicio).fechaFinProgramada(inicio.plusHours(1)).build(); }
    @Test void rechazaCruceDeCisternaConOtraAtencion() {
        when(repository.findByCisterna_IdCisterna(3)).thenReturn(List.of(otra()));
        assertEquals("La cisterna no está disponible en ese horario", assertThrows(IllegalArgumentException.class, () -> service.editar(8, request(inicio.plusHours(1)))).getMessage());
        verify(repository, never()).save(any());
    }
    @Test void rechazaCruceDeConductorConOtraAtencion() {
        when(repository.findByConductor_IdTrabajador(2)).thenReturn(List.of(otra()));
        assertEquals("El conductor no está disponible en ese horario", assertThrows(IllegalArgumentException.class, () -> service.editar(8, request(inicio.plusHours(1)))).getMessage());
        verify(repository, never()).save(any());
    }
    @Test void rechazaCisternaInactiva() {
        cisterna.setEstado(Estado.INACTIVO);
        assertThrows(IllegalArgumentException.class, () -> service.editar(8, request(inicio.plusHours(1))));
        verify(repository, never()).save(any());
    }
    @Test void inexistenteNoSeCreaPorEditar() {
        assertThrows(java.util.NoSuchElementException.class, () -> service.editar(99, request(inicio.plusHours(1))));
        verify(repository, never()).save(any());
    }
}
