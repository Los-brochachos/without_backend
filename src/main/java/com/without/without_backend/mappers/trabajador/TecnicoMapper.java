package com.without.without_backend.mappers.trabajador;

import org.springframework.stereotype.Component;

import com.without.without_backend.dto.trabajador.tecnico.TecnicoCreateRequest;
import com.without.without_backend.dto.trabajador.tecnico.TecnicoResponse;
import com.without.without_backend.entities.trabajador.Tecnico;
import com.without.without_backend.entities.trabajador.Trabajador;

@Component
public class TecnicoMapper {

    public Tecnico toEntity(
            TecnicoCreateRequest request,
            Trabajador trabajador) {

        return Tecnico.builder()
                .trabajador(trabajador)
                .especialidad(request.especialidad())
                .nivelTecnico(request.nivelTecnico())
                .build();
    }

    public TecnicoResponse toResponse(
            Tecnico tecnico) {

        Trabajador trabajador = tecnico.getTrabajador();

        return new TecnicoResponse(
                trabajador.getIdTrabajador(),
                trabajador.getNombres(),
                trabajador.getApellidos(),
                trabajador.getDni(),
                trabajador.getTelefono(),
                trabajador.getDireccion(),
                trabajador.getFechaIngreso(),
                trabajador.getEstado(),
                trabajador.getUsuario().getCorreo(),
                trabajador.getUsuario().getRol(),
                tecnico.getEspecialidad(),
                tecnico.getNivelTecnico(),
                trabajador.getFechaCreacion(),
                trabajador.getFechaActualizacion()
        );
    }
}
