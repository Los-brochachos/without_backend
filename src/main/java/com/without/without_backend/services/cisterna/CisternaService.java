package com.without.without_backend.services.cisterna;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.without.without_backend.dto.cisterna.cisterna.CisternaRequest;
import com.without.without_backend.dto.cisterna.cisterna.CisternaResponse;
import com.without.without_backend.dto.cisterna.cisterna.EditarCisternaRequest;
import com.without.without_backend.dto.cisterna.cisterna.EliminarCisternaRequest;
import com.without.without_backend.entities.cisterna.Cisterna;
import com.without.without_backend.mappers.cisterna.CisternaMapper;
import com.without.without_backend.repositories.cisterna.CisternaRepository;
import com.without.without_backend.utils.Estado;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CisternaService {

    private final CisternaRepository repository;
    private final CisternaMapper mapper;

    // LISTAR TODAS
    public List<CisternaResponse> listar() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    // LISTAR ACTIVAS
    public List<CisternaResponse> listarActivas() {
        return repository.findByEstado(Estado.ACTIVO)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    // BUSCAR POR PLACA
    public CisternaResponse buscarPorPlaca(String placa) {
        Cisterna cisterna = repository.findByPlaca(placa)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "No se encontró una cisterna con la placa: " + placa));

        return mapper.toDto(cisterna);
    }

    // BUSCAR POR NOMBRE
    public CisternaResponse buscarPorNombre(String nombre) {
        Cisterna cisterna = repository.findByNombre(nombre)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "No se encontró una cisterna con el nombre: " + nombre));

        return mapper.toDto(cisterna);
    }

    // BUSCAR POR ID
    public CisternaResponse buscarPorId(Integer id) {
        Cisterna cisterna = repository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Cisterna no encontrada"));

        return mapper.toDto(cisterna);
    }

    // BUSCAR ACTIVA POR ID
    public CisternaResponse buscarActivaPorId(Integer id) {
        Cisterna cisterna = repository
                .findByIdCisternaAndEstado(id, Estado.ACTIVO)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Cisterna no encontrada o está inactiva"));

        return mapper.toDto(cisterna);
    }

    // CREAR
    @Transactional
    public CisternaResponse crear(CisternaRequest request) {

        if (repository.existsByPlaca(request.placa())) {
            throw new IllegalArgumentException(
                    "La placa ya está registrada");
        }

        if (repository.existsByNombre(request.nombre())) {
            throw new IllegalArgumentException(
                    "El nombre de la cisterna ya está registrado");
        }

        Cisterna cisterna = mapper.toEntity(request);

        cisterna.setEstado(Estado.ACTIVO);

        Cisterna guardada = repository.save(cisterna);

        return mapper.toDto(guardada);
    }

    // EDITAR
    @Transactional
    public CisternaResponse editar(EditarCisternaRequest request) {

        Cisterna cisterna = repository
                .findByNombre(request.nombreBuscar())
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "La cisterna '" +
                                request.nombreBuscar() +
                                "' no existe"));

        // Validar placa si se desea modificar
        if (request.placa() != null
                && !request.placa().equals(cisterna.getPlaca())
                && repository.existsByPlaca(request.placa())) {

            throw new IllegalArgumentException(
                    "La placa ya está registrada");
        }

        // Validar nombre si se desea modificar
        if (request.nombre() != null && !request.nombre().equals(cisterna.getNombre()) && repository.existsByNombre(request.nombre())) {

            throw new IllegalArgumentException(
                    "El nombre de la cisterna ya está registrado");
        }

        if (request.placa() != null) {
            cisterna.setPlaca(request.placa());
        }

        if (request.nombre() != null) {
            cisterna.setNombre(request.nombre());
        }

        if (request.capacidad() != null) {
            cisterna.setCapacidad(request.capacidad());
        }

        Cisterna actualizada = repository.save(cisterna);

        return mapper.toDto(actualizada);
    }

    // ELIMINAR LÓGICAMENTE
    @Transactional
    public CisternaResponse eliminar(
            EliminarCisternaRequest request) {

        Cisterna cisterna = repository
                .findByNombre(request.nombre())
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "La cisterna '" +
                                request.nombre() +
                                "' no existe"));

        if (cisterna.getEstado() == Estado.INACTIVO) {
            throw new IllegalArgumentException(
                    "La cisterna ya está inactiva");
        }

        cisterna.setEstado(Estado.INACTIVO);

        Cisterna actualizada = repository.save(cisterna);

        return mapper.toDto(actualizada);
    }
}