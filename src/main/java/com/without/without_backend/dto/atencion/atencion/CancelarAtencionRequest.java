package com.without.without_backend.dto.atencion.atencion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelarAtencionRequest(

        @NotBlank(message = "La observación de cancelación es obligatoria")
        @Size(
            max = 250,
            message = "La observación no puede superar los 250 caracteres"
        )
        String observaciones

) {}