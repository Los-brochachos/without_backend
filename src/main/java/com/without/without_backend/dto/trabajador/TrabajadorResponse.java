package com.without.without_backend.dto.trabajador;

import java.time.LocalDate;

import com.without.without_backend.utils.Estado;
import com.without.without_backend.utils.RolesEnum;

public record TrabajadorResponse(

    Integer idTrabajador,
    String nombres,
    String apellidos,
    String dni,
    String telefono,
    String direccion,
    LocalDate fechaIngreso,
    Estado estado,
    String correo,
    RolesEnum rol

) {}