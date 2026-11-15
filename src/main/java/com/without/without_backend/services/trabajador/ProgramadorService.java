package com.without.without_backend.services.trabajador;

import com.without.without_backend.utils.PerfilSuperadmin;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.without.without_backend.dto.trabajador.programador.ProgramadorCreateRequest;
import com.without.without_backend.dto.trabajador.programador.ProgramadorResponse;
import com.without.without_backend.dto.trabajador.programador.ProgramadorUpdateRequest;
import com.without.without_backend.entities.trabajador.Programador;
import com.without.without_backend.entities.trabajador.Trabajador;
import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.mappers.trabajador.ProgramadorMapper;
import com.without.without_backend.repositories.trabajador.ProgramadorRepository;
import com.without.without_backend.repositories.trabajador.TrabajadorRepository;
import com.without.without_backend.repositories.usuario.UsuarioRepository;
import com.without.without_backend.utils.Estado;
import com.without.without_backend.utils.RolesEnum;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProgramadorService {
        
        private final ProgramadorRepository programadorRepository;
        private final TrabajadorRepository trabajadorRepository;
        private final UsuarioRepository usuarioRepository;
        private final ProgramadorMapper programadorMapper;
        private final PasswordEncoder passwordEncoder;

        public List<ProgramadorResponse> listarTodos() {
                return programadorRepository.findAll()
                        .stream()
                        .filter(x -> !PerfilSuperadmin.esSuperadmin(x.getTrabajador().getUsuario()))
                        .map(programadorMapper::toResponse)
                        .toList();
        }

        public List<ProgramadorResponse> listarActivos() {
                return programadorRepository
                        .findByTrabajadorEstado(Estado.ACTIVO)
                        .stream()
                        .filter(x -> !PerfilSuperadmin.esSuperadmin(x.getTrabajador().getUsuario()))
                        .map(programadorMapper::toResponse)
                        .toList();
        }

        public ProgramadorResponse buscarPorId(Integer id) {
                Programador programador = programadorRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Programador no encontrado"
                                ));

                return programadorMapper.toResponse(programador);
        }

        public ProgramadorResponse buscarActivoPorId(Integer id) {
                Programador programador = programadorRepository
                        .findByIdTrabajadorAndTrabajadorEstado(
                                id,
                                Estado.ACTIVO
                        )
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Programador no encontrado"
                                ));

                return programadorMapper.toResponse(programador);
        }

        @Transactional
        public ProgramadorResponse crearProgramador(
                ProgramadorCreateRequest request) {

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
                        .rol(RolesEnum.PROGRAMADOR)
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

                Programador programador =
                        programadorMapper.toEntity(
                                request,
                                trabajador
                        );

                programador = programadorRepository.save(
                        programador
                );

                return programadorMapper.toResponse(
                        programador
                );
        }

        @Transactional
        public ProgramadorResponse actualizarProgramador(
                Integer id,
                ProgramadorUpdateRequest request) {

                Programador programador = programadorRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Programador no encontrado"
                                ));
            PerfilSuperadmin.proteger(programador.getTrabajador().getUsuario());

                Trabajador trabajador = programador.getTrabajador();
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


                if (request.turno() != null) {
                        programador.setTurno(request.turno());
                }

                if (request.nivelIngles() != null) {
                        programador.setNivelIngles(request.nivelIngles());
                }

                usuarioRepository.save(usuario);
                trabajadorRepository.save(trabajador);
                programadorRepository.save(programador);

                return programadorMapper.toResponse(
                        programador
                );
        }

        @Transactional
        public void eliminarProgramador(Integer id) {

                Programador programador = programadorRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Programador no encontrado"
                                ));
            PerfilSuperadmin.proteger(programador.getTrabajador().getUsuario());

                Trabajador trabajador = programador.getTrabajador();

                trabajador.setEstado(Estado.INACTIVO);

                trabajadorRepository.save(trabajador);
        }

        public ProgramadorResponse me(String correo) {

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

                Programador programador = programadorRepository
                        .findById(trabajador.getIdTrabajador())
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Programador no encontrado"
                                )
                        );

                return programadorMapper.toResponse(programador);
        }
}
