package com.without.without_backend.dto.trabajador.programador;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.without.without_backend.utils.Estado;
import com.without.without_backend.utils.RolesEnum;
import com.without.without_backend.utils.trabajador.NivelIngles;
import com.without.without_backend.utils.trabajador.Turno;

public record ProgramadorResponse(

    // Datos del trabajador
    Integer idTrabajador,
    String nombres,
    String apellidos,
    String dni,
    String telefono,
    String direccion,
    LocalDate fechaIngreso,
    Estado estado,

    // Datos del usuario
    String correo,
    RolesEnum rol,

    // Datos del programador
    NivelIngles nivelIngles,
    Turno turno,

    // Auditoria
    LocalDateTime fechaCreacion,
    LocalDateTime fechaActualizacion
) {}
