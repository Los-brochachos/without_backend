package com.without.without_backend.dto.cisterna.cisterna;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EliminarCisternaRequest(

        @NotBlank(message = "El nombre de la cisterna es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre

) {}
