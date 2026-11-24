package com.without.without_backend.dto.usuario;

import com.without.without_backend.utils.Estado;
import com.without.without_backend.utils.RolesEnum;

public record UsuarioResponse(
    Integer idUsuario,
    String correo,
    Estado estado,
    RolesEnum rol
) {}