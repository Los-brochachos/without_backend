package com.without.without_backend.services.atencion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.without.without_backend.entities.atencion.Atencion;
import com.without.without_backend.entities.pedido.Pedido;
import com.without.without_backend.mappers.atencion.AtencionMapper;
import com.without.without_backend.repositories.atencion.AtencionRepository;
import com.without.without_backend.utils.atencion.EstadoAtencion;

class AtencionClienteTest {
    @Test void consultaSoloLasAtencionesDelCorreoAutenticado() {
        var repository = mock(AtencionRepository.class);
        var atencion = Atencion.builder().idAtencion(7).estado(EstadoAtencion.PROGRAMADA)
            .pedido(Pedido.builder().idPedido(3).build()).build();
        when(repository.findByPedido_Cliente_Usuario_Correo("cliente@without.example"))
            .thenReturn(List.of(atencion));
        var service = new AtencionService(repository, null, null, null, null, new AtencionMapper(), null);
        var resultado = service.listarMisAtenciones("cliente@without.example");
        assertEquals(1, resultado.size());
        assertEquals(7, resultado.getFirst().idAtencion());
        assertEquals(3, resultado.getFirst().idPedido());
        verify(repository).findByPedido_Cliente_Usuario_Correo("cliente@without.example");
        verify(repository, never()).findAll();
    }

    @Test void clienteSinAtencionesRecibeListaVacia() {
        var repository = mock(AtencionRepository.class);
        when(repository.findByPedido_Cliente_Usuario_Correo("otro@without.example")).thenReturn(List.of());
        var service = new AtencionService(repository, null, null, null, null, new AtencionMapper(), null);
        assertTrue(service.listarMisAtenciones("otro@without.example").isEmpty());
        verify(repository, never()).findAll();
    }
}
