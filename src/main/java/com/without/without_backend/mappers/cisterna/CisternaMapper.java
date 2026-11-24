package com.without.without_backend.mappers.cisterna;

import org.springframework.stereotype.Component;

import com.without.without_backend.dto.cisterna.cisterna.CisternaRequest;
import com.without.without_backend.dto.cisterna.cisterna.CisternaResponse;
import com.without.without_backend.entities.cisterna.Cisterna;

@Component 
public class CisternaMapper {
    public Cisterna toEntity(CisternaRequest dto){
        return Cisterna.builder()
        .placa(dto.placa())
        .nombre(dto.nombre())
        .capacidad(dto.capacidad())
        .build();
    }
    public CisternaResponse toDto(Cisterna entity){
        return new CisternaResponse(
            entity.getIdCisterna(), 
            entity.getPlaca(),
            entity.getNombre(),
            entity.getCapacidad(),
            entity.getEstado());
    }
}
