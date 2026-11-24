package com.without.without_backend.mappers.cliente;

import org.springframework.stereotype.Component;

import com.without.without_backend.dto.cliente.ClienteCreateRequest;
import com.without.without_backend.dto.cliente.ClienteResponse;
import com.without.without_backend.entities.cliente.Cliente;
import com.without.without_backend.entities.usuario.Usuario;

@Component
public class ClienteMapper {

    public Cliente toEntity(ClienteCreateRequest dto, Usuario usuario) {

        return Cliente.builder()
                .usuario(usuario)
                .ruc(dto.ruc())
                .razonSocial(dto.razonSocial())
                .direccion(dto.direccion())
                .telefono(dto.telefono())
                .build();
    }

    public ClienteResponse toDto(Cliente entity) {

        return new ClienteResponse(
                entity.getIdCliente(),
                entity.getRuc(),
                entity.getRazonSocial(),
                entity.getDireccion(),
                entity.getTelefono(),
                entity.getUsuario().getCorreo(),
                entity.getUsuario().getRol(),
                entity.getFechaCreacion(),
                entity.getFechaActualizacion(),
                entity.getUsuario().getIdUsuario(),
                entity.getUsuario().getEstado()
        );
    }
}