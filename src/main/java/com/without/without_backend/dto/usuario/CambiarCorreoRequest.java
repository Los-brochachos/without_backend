package com.without.without_backend.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** PATCH /usuarios/me: el propio usuario cambia su correo de acceso confirmando con su contraseña actual. */
public record CambiarCorreoRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo debe tener un formato válido")
        @Size(max = 100, message = "El correo no puede superar los 100 caracteres")
        String correo,
        @NotBlank(message = "Ingresa tu contraseña actual")
        String actual
) {}
