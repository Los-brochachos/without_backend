package com.without.without_backend.dto.trabajador;


import java.time.LocalDate;

import com.without.without_backend.utils.RolesEnum;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TrabajadorCreateRequest(

        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 100)
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100)
        String apellidos,

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(
                regexp = "\\d{8}",
                message = "El DNI debe contener exactamente 8 dígitos"
        )
        String dni,

        @Size(max = 20)
        String telefono,

        @Size(max = 150)
        String direccion,

        @NotNull(message = "La fecha de ingreso es obligatoria")
        LocalDate fechaIngreso,

        @NotNull(message = "El rol es obligatorio")
        RolesEnum rol,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 100)
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String contraseña

) {}