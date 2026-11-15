package com.without.without_backend.services.cisterna;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.without.without_backend.dto.cisterna.falla.CambiarEstadoFallaRequest;
import com.without.without_backend.dto.cisterna.falla.FallaRequest;
import com.without.without_backend.dto.cisterna.falla.FallaResponse;
import com.without.without_backend.entities.cisterna.Cisterna;
import com.without.without_backend.entities.cisterna.Falla;
import com.without.without_backend.entities.trabajador.Tecnico;
import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.mappers.cisterna.FallaMapper;
import com.without.without_backend.repositories.cisterna.CisternaRepository;
import com.without.without_backend.repositories.cisterna.FallaRepository;
import com.without.without_backend.repositories.cisterna.HistorialMantenimientoRepository;
import com.without.without_backend.repositories.trabajador.TecnicoRepository;
import com.without.without_backend.repositories.usuario.UsuarioRepository;
import com.without.without_backend.utils.cisterna.EstadoFalla;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FallaService {

    private final FallaRepository repository;
    private final CisternaRepository cisternaRepository;
    private final TecnicoRepository tecnicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final FallaMapper mapper;
    private final HistorialMantenimientoRepository historialRepository;

    public List<FallaResponse> listar() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<FallaResponse> findByCisternaId(Integer idCisterna) {

        if (!cisternaRepository.existsById(idCisterna)) {
            throw new NoSuchElementException("La cisterna no existe");
        }

        return repository
                .findByCisterna_IdCisterna(idCisterna)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public FallaResponse crear(
            FallaRequest request,
            String correo) {

        Usuario usuario = usuarioRepository
                .findByCorreo(correo)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Usuario no encontrado"));

        Tecnico tecnico = tecnicoRepository
                .findByTrabajadorUsuario(usuario)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Técnico no encontrado"));

        Cisterna cisterna = cisternaRepository
                .findById(request.idCisterna())
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "La cisterna no existe"));

        Falla falla = mapper.toEntity(request);

        falla.setCisterna(cisterna);
        falla.setTecnico(tecnico);
        falla.setEstado(EstadoFalla.PENDIENTE);

        Falla guardada = repository.save(falla);

        return mapper.toDto(guardada);
    }

    @Transactional
    public FallaResponse cambiarEstado(
            CambiarEstadoFallaRequest request) {

        Falla falla = repository
                .findById(request.idFalla())
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "La falla no existe"));

        EstadoFalla estadoActual = falla.getEstado();
        EstadoFalla nuevoEstado = request.estado();

        if (nuevoEstado.getOrden()
                != estadoActual.getOrden() + 1) {

            throw new IllegalArgumentException(
                    "El estado de la falla debe avanzar de forma secuencial");
        }

        // Trazabilidad: una falla solo se cierra si quedó constancia del trabajo realizado.
        if (nuevoEstado == EstadoFalla.RESUELTA
                && !historialRepository.existsByFalla_IdFalla(falla.getIdFalla())) {

            throw new IllegalArgumentException(
                    "Registra el mantenimiento realizado antes de marcar la falla como resuelta");
        }

        falla.setEstado(nuevoEstado);

        Falla actualizada = repository.save(falla);

        return mapper.toDto(actualizada);
    }
}
