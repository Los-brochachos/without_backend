package com.without.without_backend.dto.atencion.atencion;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EditarAtencionRequest(
        @NotNull(message = "El conductor es obligatorio") Integer idConductor,
        @NotNull(message = "La cisterna es obligatoria") Integer idCisterna,
        @NotNull(message = "La fecha de inicio programada es obligatoria") LocalDateTime fechaInicioProgramada,
        @NotNull(message = "La fecha de fin programada es obligatoria") LocalDateTime fechaFinProgramada,
        @Size(max = 250, message = "Las observaciones no pueden superar los 250 caracteres") String observaciones
) {}
