package com.without.without_backend.config.seed;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import com.without.without_backend.entities.atencion.Atencion;
import com.without.without_backend.entities.cliente.Cliente;
import com.without.without_backend.entities.documento.Factura;
import com.without.without_backend.entities.documento.GuiaRemision;
import com.without.without_backend.entities.pedido.*;
import com.without.without_backend.utils.Estado;
import com.without.without_backend.utils.atencion.EstadoAtencion;
import com.without.without_backend.utils.pedido.EstadoPedido;

class DatabaseSeederTest {
    private final EntityManager em = mock(EntityManager.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final List<Object> datos = new ArrayList<>();
    private <T> List<T> de(Class<T> tipo) {
        return datos.stream().filter(tipo::isInstance).map(tipo::cast).toList();
    }
    @SuppressWarnings("unchecked")
    private void preparar(long existentes) {
        TypedQuery<Long> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(existentes);
        when(encoder.encode(anyString())).thenReturn("hash");
        doAnswer(inv -> { datos.add(inv.getArgument(0)); return null; }).when(em).persist(any());
    }
    @Test void escenariosConservanEstadosFechasRecursosYDocumentosCoherentes() {
        preparar(0);
        new DatabaseSeeder(encoder, em).run();
        assertEquals(25, de(Pedido.class).size());
        assertEquals(25, de(Atencion.class).size());
        for (Cliente cliente : de(Cliente.class)) {
            var pedidos = de(Pedido.class).stream().filter(p -> p.getCliente() == cliente).toList();
            assertEquals(EnumSet.allOf(EstadoPedido.class), EnumSet.copyOf(pedidos.stream().map(Pedido::getEstado).toList()));
        }
        for (Pedido pedido : de(Pedido.class)) {
            var historial = de(SeguimientoPedido.class).stream().filter(s -> s.getPedido() == pedido).toList();
            assertEquals(pedido.getEstado(), historial.get(historial.size() - 1).getEstado());
            for (int i = 1; i < historial.size(); i++)
                assertFalse(historial.get(i).getFechaHora().isBefore(historial.get(i - 1).getFechaHora()));
            var citas = de(Atencion.class).stream().filter(a -> a.getPedido() == pedido).toList();
            long activas = citas.stream().filter(a -> a.getEstado() == EstadoAtencion.PROGRAMADA || a.getEstado() == EstadoAtencion.EN_CURSO).count();
            assertTrue(activas <= 1);
            switch (pedido.getEstado()) {
                case RECIBIDO -> { assertTrue(citas.isEmpty()); assertFalse(pedido.getFechaEntregaEstimada().isBefore(LocalDate.now(ZoneId.of("America/Lima")))); }
                case EN_PREPARACION -> { assertEquals(2, citas.size()); assertEquals(EstadoAtencion.CANCELADA, citas.get(0).getEstado()); assertEquals(EstadoAtencion.PROGRAMADA, citas.get(1).getEstado()); }
                case DESPACHADO -> assertEquals(EstadoAtencion.EN_CURSO, citas.get(0).getEstado());
                case ENTREGADO -> assertEquals(EstadoAtencion.FINALIZADA, citas.get(0).getEstado());
                case CANCELADO -> { assertEquals(0, activas); assertEquals(EstadoAtencion.CANCELADA, citas.get(0).getEstado()); }
            }
        }
        for (Atencion a : de(Atencion.class)) {
            assertEquals(Estado.ACTIVO, a.getCisterna().getEstado());
            assertTrue(a.getFechaFinProgramada().isAfter(a.getFechaInicioProgramada()));
            boolean iniciada = a.getEstado() == EstadoAtencion.EN_CURSO || a.getEstado() == EstadoAtencion.FINALIZADA;
            assertEquals(iniciada, a.getFechaInicio() != null);
            assertEquals(a.getEstado() == EstadoAtencion.FINALIZADA, a.getFechaFin() != null);
            for (Atencion b : de(Atencion.class)) {
                if (a == b || a.getEstado() == EstadoAtencion.CANCELADA || b.getEstado() == EstadoAtencion.CANCELADA) continue;
                if (a.getConductor() == b.getConductor() || a.getCisterna() == b.getCisterna())
                    assertFalse(a.getFechaInicioProgramada().isBefore(b.getFechaFinProgramada()) && b.getFechaInicioProgramada().isBefore(a.getFechaFinProgramada()));
            }
        }
        assertEquals(5, de(Factura.class).size());
        assertEquals(5, de(GuiaRemision.class).size());
        de(Factura.class).forEach(f -> assertEquals(EstadoPedido.ENTREGADO, f.getPedido().getEstado()));
        de(GuiaRemision.class).forEach(g -> assertEquals(EstadoPedido.ENTREGADO, g.getPedido().getEstado()));
        verify(em).flush();
    }
    @Test void noInsertaNiBorraSiYaExistenDatos() {
        preparar(1);
        new DatabaseSeeder(encoder, em).run();
        assertTrue(datos.isEmpty());
        verify(em, never()).flush();
        verify(em, never()).remove(any());
    }
}
