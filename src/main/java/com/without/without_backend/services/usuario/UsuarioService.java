package com.without.without_backend.services.usuario;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.without.without_backend.utils.RolesEnum;
import com.without.without_backend.dto.usuario.UsuarioResponse;
import com.without.without_backend.dto.usuario.UsuarioUpdateRequest;
import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.mappers.usuario.UsuarioMapper;
import com.without.without_backend.repositories.usuario.UsuarioRepository;
import com.without.without_backend.repositories.TokenRepository;
import com.without.without_backend.utils.Estado;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository repository;
    private final UsuarioMapper mapper;
    private final TokenRepository tokens;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return repository.findByCorreo(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    public List<UsuarioResponse> findAll() {

        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public UsuarioResponse findById(Integer id) {

        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        return mapper.toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse actualizar(Integer id, UsuarioUpdateRequest request) {

        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        protegerSuperadmin(usuario);

        String nuevo = request.correo().strip().toLowerCase();
        if (!usuario.getCorreo().equalsIgnoreCase(nuevo)
                && repository.findByCorreo(nuevo).isPresent()) {

            throw new IllegalArgumentException("El correo ya está registrado");
        }

        usuario.setCorreo(nuevo);
        usuario = repository.save(usuario);
        // Cambio de emergencia: se cierran sus sesiones (refresh tokens). Deberá ingresar con el correo nuevo.
        tokens.findByUser(usuario).ifPresent(tokens::deleteAll);

        return mapper.toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse actualizarMe(
            String correoActual,
            UsuarioUpdateRequest request) {

        Usuario usuario = repository.findByCorreo(correoActual)
                .orElseThrow(() ->
                        new NoSuchElementException("Usuario no encontrado"));

        if (!usuario.getCorreo().equals(request.correo())
                && repository.findByCorreo(request.correo()).isPresent()) {

            throw new IllegalArgumentException("El correo ya está registrado");
        }

        usuario.setCorreo(request.correo());

        return mapper.toResponse(usuario);
    }

    @Transactional
    public void desactivar(Integer id) {

        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        protegerSuperadmin(usuario);

        usuario.setEstado(Estado.INACTIVO);

        repository.save(usuario);
    }

    /** La cuenta única de SUPERADMIN no puede ser modificada ni desactivada por otro administrador. */
    private void protegerSuperadmin(Usuario usuario) {
        if (usuario.getRol() == RolesEnum.SUPERADMIN) {
            throw new IllegalArgumentException("La cuenta de superadministrador no se puede modificar ni desactivar");
        }
    }

    public UsuarioResponse me(String correo) {

        Usuario usuario = repository.findByCorreo(correo).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        return mapper.toResponse(usuario);

    }
}