package com.without.without_backend.dto.cliente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteCreateRequest(

    @NotBlank(message = "El RUC es obligatorio")
    @Pattern(
        regexp = "\\d{11}",
        message = "El RUC debe contener exactamente 11 dígitos"
    )
    String ruc,

    @NotBlank(message = "La razón social es obligatoria")
    @Size(
        max = 100,
        message = "La razón social no puede superar los 100 caracteres"
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

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    @Size(
        max = 100,
        message = "El correo no puede superar los 100 caracteres"
    )
    String correo,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(
        min = 8,
        max = 100,
        message = "La contraseña debe tener entre 8 y 100 caracteres"
    )
    String contraseña

) {}