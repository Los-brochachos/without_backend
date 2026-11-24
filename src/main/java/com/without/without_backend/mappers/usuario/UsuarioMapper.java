package com.without.without_backend.mappers.usuario;

import org.springframework.stereotype.Component;

import com.without.without_backend.dto.usuario.UsuarioResponse;
import com.without.without_backend.entities.usuario.Usuario;

@Component
public class UsuarioMapper {

    public UsuarioResponse toResponse(Usuario usuario) {

        return new UsuarioResponse(
                usuario.getIdUsuario(),
                usuario.getCorreo(),
                usuario.getEstado(),
                usuario.getRol()
        );
    }
}