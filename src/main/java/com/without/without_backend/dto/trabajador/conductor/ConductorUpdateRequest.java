package com.without.without_backend.dto.trabajador.conductor;

import java.time.LocalDate;

import com.without.without_backend.utils.trabajador.CategoriaLicencia;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ConductorUpdateRequest(

    // Usuario
    @Email(message = "El correo no tiene un formato válido")
    @Size(
        max = 100,
        message = "El correo no puede superar los 100 caracteres"
    )
    String correo,

    // Trabajador
    @Size(
        max = 100,
        message = "Los nombres no pueden superar los 100 caracteres"
    )
    String nombres,

    @Size(
        max = 100,
        message = "Los apellidos no pueden superar los 100 caracteres"
    )
    String apellidos,

    @Pattern(
        regexp = "\\d{8}",
        message = "El DNI debe tener 8 dígitos"
    )
    String dni,

    @Size(
        max = 20,
        message = "El teléfono no puede superar los 20 caracteres"
    )
    String telefono,

    @Size(
        max = 150,
        message = "La dirección no puede superar los 150 caracteres"
    )
    String direccion,

    LocalDate fechaIngreso,

    // Conductor
    @Size(
        max = 30,
        message = "La licencia de conducir no puede superar los 30 caracteres"
    )
    String licenciaConducir,

    @NotNull (message = "La categoría de licencia es obligatoria")
    CategoriaLicencia categoriaLicencia,

    @Future(
        message = "La fecha de vencimiento debe ser posterior a la fecha actual"
    )
    LocalDate fechaVencimientoLicencia

) {}

