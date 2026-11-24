package com.without.without_backend.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarClaveRequest(
        @NotBlank(message = "Ingresa tu contraseña actual") String actual,
        @NotBlank(message = "Ingresa la nueva contraseña")
        @Size(min = 8, max = 72, message = "La nueva contraseña debe tener entre 8 y 72 caracteres") String nueva
) {}
