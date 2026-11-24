package com.without.without_backend.dto.trabajador;

import java.time.LocalDate;

import com.without.without_backend.utils.Estado;
import com.without.without_backend.utils.RolesEnum;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TrabajadorRequest(

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Los nombres no pueden superar los 100 caracteres")
    String nombres,

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden superar los 100 caracteres")
    String apellidos,

    @NotBlank(message = "El DNI es obligatorio")
    @Size(max = 20, message = "El DNI no puede superar los 20 caracteres")
    String dni,

    @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
    String telefono,

    @Size(max = 150, message = "La dirección no puede superar los 150 caracteres")
    String direccion,

    @NotNull(message = "La fecha de ingreso es obligatoria")
    LocalDate fechaIngreso,

    @NotNull(message = "El estado es obligatorio")
    Estado estado,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    @Size(max = 100, message = "El correo no puede superar los 100 caracteres")
    String correo,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener mínimo 6 caracteres")
    String contraseña,

    @NotNull(message = "El rol es obligatorio")
    RolesEnum rol

) {}