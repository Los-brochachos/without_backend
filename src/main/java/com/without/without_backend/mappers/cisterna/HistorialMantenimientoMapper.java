package com.without.without_backend.mappers.cisterna;

import org.springframework.stereotype.Component;

import com.without.without_backend.dto.cisterna.historialmantenimiento.HistorialMantenimientoRequest;
import com.without.without_backend.dto.cisterna.historialmantenimiento.HistorialMantenimientoResponse;
import com.without.without_backend.entities.cisterna.Falla;
import com.without.without_backend.entities.cisterna.HistorialMantenimiento;

@Component
public class HistorialMantenimientoMapper {

    public HistorialMantenimiento toEntity(
            HistorialMantenimientoRequest dto) {

        if (dto == null) {
            return null;
        }

        Falla falla = Falla.builder()
                .idFalla(dto.idFalla())
                .build();

        return HistorialMantenimiento.builder()
                .fecha(dto.fecha())
                .descripcion(dto.descripcion())
                .resultado(dto.resultado())
                .observaciones(dto.observaciones())
                .costo(dto.costo())
                .falla(falla)
                .build();
    }

    public HistorialMantenimientoResponse toDto(
            HistorialMantenimiento entity) {

        if (entity == null) {
            return null;
        }

        Falla falla = entity.getFalla();

        return new HistorialMantenimientoResponse(
                entity.getIdHistorial(),
                entity.getFecha(),
                entity.getDescripcion(),
                entity.getResultado(),
                entity.getObservaciones(),
                entity.getCosto(),

                falla != null
                        ? falla.getIdFalla()
                        : null,

                falla != null && falla.getCisterna() != null
                        ? falla.getCisterna().getIdCisterna()
                        : null,

                falla != null && falla.getTecnico() != null
                        ? falla.getTecnico().getIdTrabajador()
                        : null
        );
    }
}