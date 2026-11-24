package com.without.without_backend.dto.trabajador.programador;

import java.time.LocalDate;

import com.without.without_backend.utils.trabajador.NivelIngles;
import com.without.without_backend.utils.trabajador.Turno;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProgramadorUpdateRequest(

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

    // Datos de Programador
    Turno turno,

    NivelIngles nivelIngles

) {}