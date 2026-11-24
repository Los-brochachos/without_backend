package com.without.without_backend.dto.trabajador.conductor;

import java.time.LocalDate;

import com.without.without_backend.utils.trabajador.CategoriaLicencia;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ConductorCreateRequest(

    // Usuario
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 100, message = "El correo no puede superar los 100 caracteres")
    String correo,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(
        min = 8,
        max = 100,
        message = "La contraseña debe tener entre 8 y 100 caracteres"
    )
    String contraseña,

    // Trabajador
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

    // Conductor
    @NotBlank(message = "La licencia de conducir es obligatoria")
    @Size(
        max = 30,
        message = "La licencia de conducir no puede superar los 30 caracteres"
    )
    String licenciaConducir,

    @NotNull(message = "La categoría de licencia es obligatoria")
    CategoriaLicencia categoriaLicencia,

    @NotNull(message = "La fecha de vencimiento de la licencia es obligatoria")
    @Future(message = "La fecha de vencimiento debe ser posterior a la fecha actual")
    LocalDate fechaVencimientoLicencia

) {}