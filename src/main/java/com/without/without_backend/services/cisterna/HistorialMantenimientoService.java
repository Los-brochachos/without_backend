package com.without.without_backend.services.cisterna;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.without.without_backend.dto.cisterna.historialmantenimiento.HistorialMantenimientoRequest;
import com.without.without_backend.dto.cisterna.historialmantenimiento.HistorialMantenimientoResponse;
import com.without.without_backend.entities.cisterna.Falla;
import com.without.without_backend.entities.cisterna.HistorialMantenimiento;
import com.without.without_backend.mappers.cisterna.HistorialMantenimientoMapper;
import com.without.without_backend.repositories.cisterna.FallaRepository;
import com.without.without_backend.repositories.cisterna.HistorialMantenimientoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistorialMantenimientoService {

    private final HistorialMantenimientoRepository repository;
    private final FallaRepository fallaRepository;
    private final HistorialMantenimientoMapper mapper;

    public List<HistorialMantenimientoResponse> listar() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<HistorialMantenimientoResponse> buscarPorCisterna(
            Integer idCisterna) {

        return repository
                .findByFalla_Cisterna_IdCisterna(idCisterna)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<HistorialMantenimientoResponse> buscarPorNombreCisterna(
            String nombre) {

        return repository
                .findByFalla_Cisterna_Nombre(nombre)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<HistorialMantenimientoResponse> buscarPorFecha(
            LocalDate fecha) {

        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.plusDays(1).atStartOfDay();

        return repository
                .findByFechaGreaterThanEqualAndFechaLessThan(
                        inicioDia,
                        finDia
                )
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public HistorialMantenimientoResponse crear(
            HistorialMantenimientoRequest request) {

        Falla falla = fallaRepository
                .findById(request.idFalla())
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "La falla no existe"));

        HistorialMantenimiento historial =
                mapper.toEntity(request);

        historial.setFalla(falla);

        HistorialMantenimiento guardado =
                repository.save(historial);

        return mapper.toDto(guardado);
    }
}