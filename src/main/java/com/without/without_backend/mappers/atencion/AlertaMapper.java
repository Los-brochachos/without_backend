package com.without.without_backend.mappers.atencion;

import org.springframework.stereotype.Component;

import com.without.without_backend.dto.atencion.alerta.AlertaRequest;
import com.without.without_backend.dto.atencion.alerta.AlertaResponse;
import com.without.without_backend.entities.atencion.Alerta;
import com.without.without_backend.entities.atencion.Atencion;

@Component
public class AlertaMapper {

    public Alerta toEntity(AlertaRequest dto) {

        if (dto == null) {
            return null;
        }

        Atencion atencion = Atencion.builder()
                .idAtencion(dto.idAtencion())
                .build();

        return Alerta.builder()
                .tipo(dto.tipo())
                .mensaje(dto.mensaje())
                .atencion(atencion)
                .build();
    }

    public AlertaResponse toDto(Alerta entity) {

        if (entity == null) {
            return null;
        }

        return new AlertaResponse(
                entity.getIdAlerta(),
                entity.getTipo(),
                entity.getMensaje(),
                entity.getFechaHora(),
                entity.getEstado(),
                entity.getAtencion() != null
                        ? entity.getAtencion().getIdAtencion()
                        : null
        );
    }
}