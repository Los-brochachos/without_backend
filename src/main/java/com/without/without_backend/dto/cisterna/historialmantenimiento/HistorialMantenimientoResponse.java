package com.without.without_backend.dto.cisterna.historialmantenimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistorialMantenimientoResponse(

        Integer idHistorial,

        LocalDateTime fecha,

        String descripcion,

        String resultado,

        String observaciones,

        BigDecimal costo,

        Integer idFalla,

        Integer idCisterna,

        Integer idTecnico

) {}