package com.without.without_backend.dto.cisterna.historialmantenimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HistorialMantenimientoRequest(

        @NotNull(message = "La fecha del mantenimiento es obligatoria")
        LocalDateTime fecha,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 250, message = "La descripción no puede superar los 250 caracteres")
        String descripcion,

        @Size(max = 250, message = "El resultado no puede superar los 250 caracteres")
        String resultado,

        @Size(max = 250, message = "Las observaciones no pueden superar los 250 caracteres")
        String observaciones,

        @DecimalMin(value = "0.00", message = "El costo no puede ser negativo")
        BigDecimal costo,

        @NotNull(message = "La falla es obligatoria")
        Integer idFalla

) {}