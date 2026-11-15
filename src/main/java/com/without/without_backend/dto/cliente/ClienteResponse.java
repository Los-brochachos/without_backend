package com.without.without_backend.dto.cliente;

import java.time.LocalDateTime;

import com.without.without_backend.utils.RolesEnum;
import com.without.without_backend.utils.Estado;

public record ClienteResponse(

    Integer idCliente,

    String ruc,

    String razonSocial,

    String direccion,

    String telefono,

    String correo,

    RolesEnum rol,

    LocalDateTime fechaCreacion,

    LocalDateTime fechaActualizacion,

    /** Cuenta de acceso del cliente (para activar/desactivar desde Usuarios). */
    Integer idUsuario,

    /** ACTIVO / INACTIVO: estado de la cuenta del cliente (DELETE /clientes/{id} la desactiva). */
    Estado estado
) {}