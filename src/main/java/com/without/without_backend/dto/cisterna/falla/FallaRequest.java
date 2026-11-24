package com.without.without_backend.dto.cisterna.falla;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FallaRequest(

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 250, message = "La descripción no puede superar los 250 caracteres")
        String descripcion,

        @NotNull(message = "La fecha y hora de la falla son obligatorias")
        LocalDateTime fechaHora,

        @NotNull(message = "La cisterna es obligatoria")
        Integer idCisterna
) {}