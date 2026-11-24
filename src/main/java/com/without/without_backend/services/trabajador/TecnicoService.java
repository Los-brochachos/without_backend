package com.without.without_backend.services.trabajador;

import com.without.without_backend.utils.PerfilSuperadmin;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.without.without_backend.dto.trabajador.tecnico.TecnicoCreateRequest;
import com.without.without_backend.dto.trabajador.tecnico.TecnicoResponse;
import com.without.without_backend.dto.trabajador.tecnico.TecnicoUpdateRequest;
import com.without.without_backend.entities.trabajador.Tecnico;
import com.without.without_backend.entities.trabajador.Trabajador;
import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.mappers.trabajador.TecnicoMapper;
import com.without.without_backend.repositories.trabajador.TecnicoRepository;
import com.without.without_backend.repositories.trabajador.TrabajadorRepository;
import com.without.without_backend.repositories.usuario.UsuarioRepository;
import com.without.without_backend.utils.Estado;
import com.without.without_backend.utils.RolesEnum;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TecnicoService {

        private final TecnicoRepository tecnicoRepository;
        private final TrabajadorRepository trabajadorRepository;
        private final UsuarioRepository usuarioRepository;
        private final TecnicoMapper tecnicoMapper;
        private final PasswordEncoder passwordEncoder;

        public List<TecnicoResponse> listarTodos() {
                return tecnicoRepository.findAll()
                        .stream()
                        .filter(x -> !PerfilSuperadmin.esSuperadmin(x.getTrabajador().getUsuario()))
                        .map(tecnicoMapper::toResponse)
                        .toList();
        }

        public List<TecnicoResponse> listarActivos() {
                return tecnicoRepository
                        .findByTrabajadorEstado(Estado.ACTIVO)
                        .stream()
                        .filter(x -> !PerfilSuperadmin.esSuperadmin(x.getTrabajador().getUsuario()))
                        .map(tecnicoMapper::toResponse)
                        .toList();
        }

        public TecnicoResponse buscarPorId(Integer id) {
                Tecnico tecnico = tecnicoRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Técnico no encontrado"
                                ));

                return tecnicoMapper.toResponse(tecnico);
        }

        public TecnicoResponse buscarActivoPorId(Integer id) {
                Tecnico tecnico = tecnicoRepository
                        .findByIdTrabajadorAndTrabajadorEstado(
                                id,
                                Estado.ACTIVO
                        )
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Técnico no encontrado"
                                ));

                return tecnicoMapper.toResponse(tecnico);
        }

        @Transactional
        public TecnicoResponse crearTecnico(
                TecnicoCreateRequest request) {

                if (usuarioRepository
                        .findByCorreo(request.correo())
                        .isPresent()) {

                throw new IllegalArgumentException(
                        "El correo ya está registrado"
                );
                }

                if (trabajadorRepository
                        .findByDni(request.dni())
                        .isPresent()) {

                throw new IllegalArgumentException(
                        "El DNI ya está registrado"
                );
                }

                Usuario usuario = Usuario.builder()
                        .correo(request.correo())
                        .contraseña(
                                passwordEncoder.encode(
                                        request.contraseña()
                                )
                        )
                        .estado(Estado.ACTIVO)
                        .rol(RolesEnum.TECNICO)
                        .build();

                usuario = usuarioRepository.save(usuario);

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

                Tecnico tecnico = tecnicoMapper.toEntity(
                        request,
                        trabajador
                );

                tecnico = tecnicoRepository.save(tecnico);

                return tecnicoMapper.toResponse(tecnico);
        }

        @Transactional
        public TecnicoResponse actualizarTecnico(
                Integer id,
                TecnicoUpdateRequest request) {

                Tecnico tecnico = tecnicoRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Técnico no encontrado"
                                ));
            PerfilSuperadmin.proteger(tecnico.getTrabajador().getUsuario());

                Trabajador trabajador = tecnico.getTrabajador();
                Usuario usuario = trabajador.getUsuario();

                if (request.nombres() != null) {
                trabajador.setNombres(request.nombres());
                }

                if (request.apellidos() != null) {
                trabajador.setApellidos(request.apellidos());
                }

                if (request.dni() != null
                        && !trabajador.getDni().equals(request.dni())) {

                if (trabajadorRepository
                        .findByDni(request.dni())
                        .isPresent()) {

                        throw new IllegalArgumentException(
                                "El DNI ya está registrado"
                        );
                }

                trabajador.setDni(request.dni());
                }

                if (request.telefono() != null) {
                trabajador.setTelefono(request.telefono());
                }

                if (request.direccion() != null) {
                trabajador.setDireccion(request.direccion());
                }

                if (request.fechaIngreso() != null) {
                trabajador.setFechaIngreso(
                        request.fechaIngreso()
                );
                }

                if (request.correo() != null
                        && !usuario.getCorreo()
                                .equals(request.correo())) {

                if (usuarioRepository
                        .findByCorreo(request.correo())
                        .isPresent()) {

                        throw new IllegalArgumentException(
                                "El correo ya está registrado"
                        );
                }

                usuario.setCorreo(request.correo());
                }

                if (request.especialidad() != null) {
                tecnico.setEspecialidad(
                        request.especialidad()
                );
                }

                if (request.nivelTecnico() != null) {
                tecnico.setNivelTecnico(
                        request.nivelTecnico()
                );
                }



                usuarioRepository.save(usuario);
                trabajadorRepository.save(trabajador);
                tecnicoRepository.save(tecnico);

                return tecnicoMapper.toResponse(tecnico);
        }

        @Transactional
        public void eliminarTecnico(Integer id) {

                Tecnico tecnico = tecnicoRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Técnico no encontrado"
                                ));
            PerfilSuperadmin.proteger(tecnico.getTrabajador().getUsuario());

                Trabajador trabajador = tecnico.getTrabajador();

                trabajador.setEstado(Estado.INACTIVO);

                trabajadorRepository.save(trabajador);
        }

        public TecnicoResponse me(String correo) {

                Usuario usuario = usuarioRepository.findByCorreo(correo)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Usuario no encontrado"
                                )
                        );

                Trabajador trabajador = trabajadorRepository
                        .findByUsuario(usuario)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Trabajador no encontrado"
                                )
                        );

                Tecnico tecnico = tecnicoRepository
                        .findById(trabajador.getIdTrabajador())
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Técnico no encontrado"
                                )
                        );

                return tecnicoMapper.toResponse(tecnico);
        }
}
