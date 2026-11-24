package com.without.without_backend.dto.cliente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteUpdateRequest(

    @Pattern(
        regexp = "\\d{11}",
        message = "El RUC debe tener 11 caracteres"
    )
    String ruc,

    @Size(
        max = 100,
        message = "La Razón Social no puede superar los 100 caracteres"
    )
    String razonSocial,

    @Size(
        max = 150,
        message = "La dirección no puede superar los 150 caracteres"
    )
    String direccion,

    @Size(
        max = 20,
        message = "El teléfono no puede superar los 20 caracteres"
    )
    String telefono,

    @Email(
        message = "El correo no tiene un formato válido"
    )
    @Size(
        max = 100,
        message = "El correo no puede superar los 100 caracteres"
    )
    String correo,

    @Size(
        min = 6,
        max = 100,
        message = "La contraseña debe tener entre 6 y 100 caracteres"
    )
    String contraseña

) {}