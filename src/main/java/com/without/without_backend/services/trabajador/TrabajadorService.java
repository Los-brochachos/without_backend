package com.without.without_backend.services.trabajador;

import com.without.without_backend.utils.PerfilSuperadmin;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.without.without_backend.dto.trabajador.TrabajadorCreateRequest;
import com.without.without_backend.dto.trabajador.TrabajadorResponse;
import com.without.without_backend.dto.trabajador.TrabajadorUpdateRequest;
import com.without.without_backend.entities.trabajador.Trabajador;
import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.mappers.trabajador.TrabajadorMapper;
import com.without.without_backend.repositories.trabajador.TrabajadorRepository;
import com.without.without_backend.repositories.usuario.UsuarioRepository;
import com.without.without_backend.utils.Estado;
import com.without.without_backend.utils.RolesEnum;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrabajadorService {

    private final TrabajadorRepository trabajadorRepository;
    private final TrabajadorMapper trabajadorMapper;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public TrabajadorResponse me(String correo) {
        Trabajador trabajador = trabajadorRepository.findByUsuario_Correo(correo)
                .orElseThrow(() -> new NoSuchElementException("No se encontró el trabajador asociado a tu cuenta"));
        return trabajadorMapper.toResponse(trabajador);
    }

    @Transactional(readOnly = true)
    public List<TrabajadorResponse> listarTodos() {

        return trabajadorRepository
                .findAll()
                .stream()
                .filter(x -> !PerfilSuperadmin.esSuperadmin(x.getUsuario()))
                .map(trabajadorMapper::toResponse)
                .toList();
    }

    public List<TrabajadorResponse> listarActivos() {

        return trabajadorRepository
                .findByEstado(Estado.ACTIVO)
                .stream()
                .filter(x -> !PerfilSuperadmin.esSuperadmin(x.getUsuario()))
                .map(trabajadorMapper::toResponse)
                .toList();
    }

    public TrabajadorResponse buscarPorId(Integer id) {

        Trabajador trabajador = trabajadorRepository
                .findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Trabajador no encontrado"
                        ));

        return trabajadorMapper.toResponse(trabajador);
    }


    @Transactional 
    public TrabajadorResponse crear(TrabajadorCreateRequest request) {

    if (request.rol() != RolesEnum.ANALISTA
            && request.rol() != RolesEnum.ADMIN) {
        throw new IllegalArgumentException(
                "Solo se permite registrar ADMIN o ANALISTA"
        );
    }

    if (usuarioRepository.findByCorreo(request.correo()).isPresent()) {
        throw new IllegalArgumentException("El correo ya está registrado");
    }
        
    

    if (trabajadorRepository.findByDni(request.dni()).isPresent()) {
        throw new IllegalArgumentException("El DNI ya está registrado");
    }
    

    Usuario usuario = Usuario.builder()
            .correo(request.correo())
            .contraseña(passwordEncoder.encode(request.contraseña()))
            .rol(request.rol())
            .estado(Estado.ACTIVO)
            .build();

    usuarioRepository.save(usuario);

    Trabajador trabajador = Trabajador.builder()
            .nombres(request.nombres())
            .apellidos(request.apellidos())
            .dni(request.dni())
            .telefono(request.telefono())
            .direccion(request.direccion())
            .fechaIngreso(request.fechaIngreso())
            .estado(Estado.ACTIVO)
            .usuario(usuario)
            .build();

    trabajador = trabajadorRepository.save(trabajador);

    return trabajadorMapper.toResponse(trabajador);
}
    @Transactional
    public TrabajadorResponse actualizar(Integer id, TrabajadorUpdateRequest request) {
        Trabajador trabajador = trabajadorRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Trabajador no encontrado"));
        PerfilSuperadmin.proteger(trabajador.getUsuario());
        trabajadorRepository.findByDni(request.dni()).ifPresent(otro -> {
            if (!otro.getIdTrabajador().equals(id)) throw new IllegalArgumentException("El DNI ya está registrado");
        });
        trabajador.setNombres(request.nombres().strip());
        trabajador.setApellidos(request.apellidos().strip());
        trabajador.setDni(request.dni());
        trabajador.setTelefono(request.telefono());
        trabajador.setDireccion(request.direccion());
        trabajador.setFechaIngreso(request.fechaIngreso());
        return trabajadorMapper.toResponse(trabajadorRepository.save(trabajador));
    }

    @Transactional
    public TrabajadorResponse cambiarActivo(Integer id, boolean activo, String correoActual) {
        Trabajador trabajador = trabajadorRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Trabajador no encontrado"));
        PerfilSuperadmin.proteger(trabajador.getUsuario());
        if (!activo && trabajador.getUsuario().getCorreo().equalsIgnoreCase(correoActual)) {
            throw new IllegalArgumentException("No puedes desactivar tu propia cuenta");
        }
        if (!activo && trabajador.getUsuario().getRol() == RolesEnum.CONDUCTOR
                && trabajadorRepository.contarAtencionesActivas(id, List.of(
                    com.without.without_backend.utils.atencion.EstadoAtencion.PROGRAMADA,
                    com.without.without_backend.utils.atencion.EstadoAtencion.EN_CURSO)) > 0) {
            throw new IllegalArgumentException("El conductor tiene atenciones activas. Reasígnalas o finalízalas antes de desactivarlo");
        }
        Estado estado = activo ? Estado.ACTIVO : Estado.INACTIVO;
        trabajador.setEstado(estado);
        trabajador.getUsuario().setEstado(estado);
        usuarioRepository.save(trabajador.getUsuario());
        return trabajadorMapper.toResponse(trabajadorRepository.save(trabajador));
    }

    public TrabajadorResponse buscarActivoPorId(Integer id) {

        Trabajador trabajador = trabajadorRepository
                .findByIdTrabajadorAndEstado(
                        id,
                        Estado.ACTIVO
                )
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Trabajador no encontrado"
                        ));

        return trabajadorMapper.toResponse(trabajador);
    }

    public TrabajadorResponse buscarPorDni(String dni) {

        Trabajador trabajador = trabajadorRepository
                .findByDni(dni)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Trabajador no encontrado"
                        ));

        return trabajadorMapper.toResponse(trabajador);
    }

    public TrabajadorResponse buscarActivoPorDni(String dni) {

        Trabajador trabajador = trabajadorRepository
                .findByDniAndEstado(
                        dni,
                        Estado.ACTIVO
                )
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Trabajador no encontrado"
                        ));

        return trabajadorMapper.toResponse(trabajador);
    }
}
