package com.without.without_backend.dto.cisterna.falla;

import com.without.without_backend.utils.cisterna.EstadoFalla;

import jakarta.validation.constraints.NotNull;

public record CambiarEstadoFallaRequest(

        @NotNull(message = "El ID de la falla es obligatorio")
        Integer idFalla,

        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoFalla estado

) {}