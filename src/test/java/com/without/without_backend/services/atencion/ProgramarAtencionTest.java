package com.without.without_backend.services.atencion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.without.without_backend.dto.atencion.atencion.AtencionRequest;
import com.without.without_backend.entities.atencion.Atencion;
import com.without.without_backend.entities.cisterna.Cisterna;
import com.without.without_backend.entities.pedido.Pedido;
import com.without.without_backend.entities.trabajador.Conductor;
import com.without.without_backend.entities.trabajador.Programador;
import com.without.without_backend.entities.trabajador.Trabajador;
import com.without.without_backend.mappers.atencion.AtencionMapper;
import com.without.without_backend.repositories.atencion.AtencionRepository;
import com.without.without_backend.repositories.cisterna.CisternaRepository;
import com.without.without_backend.repositories.pedido.PedidoRepository;
import com.without.without_backend.repositories.trabajador.ConductorRepository;
import com.without.without_backend.repositories.trabajador.ProgramadorRepository;
import com.without.without_backend.utils.Estado;
import com.without.without_backend.utils.atencion.EstadoAtencion;
import com.without.without_backend.utils.pedido.EstadoPedido;

class ProgramarAtencionTest {
    private final AtencionRepository repository = mock(AtencionRepository.class);
    private final PedidoRepository pedidos = mock(PedidoRepository.class);
    private final ConductorRepository conductores = mock(ConductorRepository.class);
    private final CisternaRepository cisternas = mock(CisternaRepository.class);
    private final ProgramadorRepository programadores = mock(ProgramadorRepository.class);
    private final com.without.without_backend.repositories.pedido.SeguimientoPedidoRepository seguimientos = mock(com.without.without_backend.repositories.pedido.SeguimientoPedidoRepository.class);
    private final AtencionService service = new AtencionService(repository, pedidos, conductores, cisternas, programadores, new AtencionMapper(), seguimientos);
    private final LocalDateTime inicio = LocalDateTime.of(2027, 1, 10, 10, 0);
    private final Pedido pedido = Pedido.builder().idPedido(1).estado(EstadoPedido.RECIBIDO).build();
    private AtencionRequest request(LocalDateTime fin) { return new AtencionRequest(1, 2, 3, inicio, fin, null); }
    private Atencion ocupada(LocalDateTime desde, LocalDateTime hasta) { return Atencion.builder().estado(EstadoAtencion.PROGRAMADA).fechaInicioProgramada(desde).fechaFinProgramada(hasta).build(); }
    @BeforeEach void preparar() {
        when(programadores.findByTrabajadorUsuarioCorreo("p@ejemplo.com")).thenReturn(Optional.of(Programador.builder().idTrabajador(4).build()));
        when(pedidos.findById(1)).thenReturn(Optional.of(pedido));
        var trabajador = mock(Trabajador.class); when(trabajador.getEstado()).thenReturn(Estado.ACTIVO);
        var conductor = mock(Conductor.class); when(conductor.getTrabajador()).thenReturn(trabajador);
        when(conductores.findById(2)).thenReturn(Optional.of(conductor));
        when(cisternas.findById(3)).thenReturn(Optional.of(Cisterna.builder().idCisterna(3).estado(Estado.ACTIVO).build()));
    }
    @Test void rechazaPedidoCancelado() {
        pedido.setEstado(EstadoPedido.CANCELADO);
        assertThrows(IllegalArgumentException.class, () -> service.crear(request(inicio.plusHours(1)), "p@ejemplo.com"));
        verify(repository, never()).save(any());
    }
    @Test void rechazaSegundaAtencionActiva() {
        when(repository.findByPedidoIdPedido(1)).thenReturn(List.of(ocupada(inicio, inicio.plusHours(1))));
        assertEquals("El pedido ya tiene una atención activa", assertThrows(IllegalArgumentException.class, () -> service.crear(request(inicio.plusHours(1)), "p@ejemplo.com")).getMessage());
        verify(repository, never()).save(any());
    }
    @Test void rechazaDuracionCero() {
        assertThrows(IllegalArgumentException.class, () -> service.crear(request(inicio), "p@ejemplo.com"));
        verify(repository, never()).save(any());
    }
    @Test void rechazaCruceDeCisterna() {
        when(repository.findByCisterna_IdCisterna(3)).thenReturn(List.of(ocupada(inicio.minusMinutes(30), inicio.plusMinutes(30))));
        assertEquals("La cisterna no está disponible en ese horario", assertThrows(IllegalArgumentException.class, () -> service.crear(request(inicio.plusHours(1)), "p@ejemplo.com")).getMessage());
        verify(repository, never()).save(any());
    }
    @Test void rechazaCruceDeConductor() {
        when(repository.findByConductor_IdTrabajador(2)).thenReturn(List.of(ocupada(inicio, inicio.plusHours(2))));
        assertEquals("El conductor no está disponible en ese horario", assertThrows(IllegalArgumentException.class, () -> service.crear(request(inicio.plusHours(1)), "p@ejemplo.com")).getMessage());
        verify(repository, never()).save(any());
    }
    @Test void permiteIntervalosConsecutivos() {
        when(repository.findByCisterna_IdCisterna(3)).thenReturn(List.of(ocupada(inicio.minusHours(1), inicio)));
        when(repository.save(any(Atencion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var resultado = service.crear(request(inicio.plusHours(1)), "p@ejemplo.com");
        assertEquals(EstadoAtencion.PROGRAMADA, resultado.estado());
        assertEquals(1, resultado.idPedido());
        assertEquals(EstadoPedido.EN_PREPARACION, pedido.getEstado());
        verify(pedidos).save(pedido);
        verify(seguimientos).save(argThat(s -> s.getPedido() == pedido && s.getEstado() == EstadoPedido.EN_PREPARACION));
        verify(repository).save(any(Atencion.class));
    }
}
