package com.without.without_backend.services.atencion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.without.without_backend.dto.atencion.alerta.AlertaRequest;
import com.without.without_backend.entities.atencion.Alerta;
import com.without.without_backend.entities.atencion.Atencion;
import com.without.without_backend.entities.trabajador.Conductor;
import com.without.without_backend.mappers.atencion.AlertaMapper;
import com.without.without_backend.repositories.atencion.AlertaRepository;
import com.without.without_backend.repositories.atencion.AtencionRepository;
import com.without.without_backend.repositories.trabajador.ConductorRepository;
import com.without.without_backend.utils.atencion.EstadoAlerta;
import com.without.without_backend.utils.atencion.EstadoAtencion;
import com.without.without_backend.utils.atencion.TipoAlerta;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertaService: el conductor solo reporta y consulta alertas de sus atenciones")
class AlertaConductorTest {
    private static final String CORREO = "conductor1@example.test";
    @Mock AlertaRepository alertas;
    @Mock AtencionRepository atenciones;
    @Mock ConductorRepository conductores;
    AlertaService service;
    Conductor yo, otro;

    @BeforeEach void preparar() {
        service = new AlertaService(alertas, atenciones, conductores, new AlertaMapper());
        yo = Conductor.builder().idTrabajador(3).build();
        otro = Conductor.builder().idTrabajador(9).build();
    }

    private Atencion atencion(Conductor conductor, EstadoAtencion estado) {
        return Atencion.builder().idAtencion(7).conductor(conductor).estado(estado).build();
    }

    @Test void misAlertasFiltraPorElCorreoDelConductor() {
        Alerta a = Alerta.builder().idAlerta(1).tipo(TipoAlerta.RETRASO).mensaje("x").estado(EstadoAlerta.PENDIENTE)
            .atencion(atencion(yo, EstadoAtencion.EN_CURSO)).build();
        when(alertas.findByAtencionConductorTrabajadorUsuarioCorreoOrderByFechaHoraDesc(CORREO)).thenReturn(List.of(a));
        assertEquals(1, service.listarMisAlertas(CORREO).size());
        verify(alertas, never()).findAll();
    }

    @Test void alertasDeAtencionAjenaResponden404SinRevelarla() {
        when(atenciones.findById(7)).thenReturn(Optional.of(atencion(otro, EstadoAtencion.EN_CURSO)));
        when(conductores.findByTrabajadorUsuarioCorreo(CORREO)).thenReturn(Optional.of(yo));
        var error = assertThrows(NoSuchElementException.class, () -> service.listarPorAtencionDelConductor(7, CORREO));
        assertEquals(AlertaService.ATENCION_NO_DISPONIBLE, error.getMessage());
        verify(alertas, never()).findByAtencionIdAtencion(any());
    }

    @Test void alertasDeAtencionPropiaSeListan() {
        when(atenciones.findById(7)).thenReturn(Optional.of(atencion(yo, EstadoAtencion.EN_CURSO)));
        when(conductores.findByTrabajadorUsuarioCorreo(CORREO)).thenReturn(Optional.of(yo));
        when(atenciones.existsById(7)).thenReturn(true);
        when(alertas.findByAtencionIdAtencion(7)).thenReturn(List.of());
        assertTrue(service.listarPorAtencionDelConductor(7, CORREO).isEmpty());
    }

    @Test void noSeReportaEnAtencionQueNoEstaEnCurso() {
        when(atenciones.findById(7)).thenReturn(Optional.of(atencion(yo, EstadoAtencion.PROGRAMADA)));
        when(conductores.findByTrabajadorUsuarioCorreo(CORREO)).thenReturn(Optional.of(yo));
        assertThrows(IllegalArgumentException.class,
            () -> service.crear(new AlertaRequest(TipoAlerta.RETRASO, "Trafico", 7), CORREO));
        verify(alertas, never()).save(any());
    }

    @Test void noSeReportaEnAtencionDeOtroConductor() {
        when(atenciones.findById(7)).thenReturn(Optional.of(atencion(otro, EstadoAtencion.EN_CURSO)));
        when(conductores.findByTrabajadorUsuarioCorreo(CORREO)).thenReturn(Optional.of(yo));
        assertThrows(IllegalArgumentException.class,
            () -> service.crear(new AlertaRequest(TipoAlerta.RETRASO, "Trafico", 7), CORREO));
        verify(alertas, never()).save(any());
    }
}
