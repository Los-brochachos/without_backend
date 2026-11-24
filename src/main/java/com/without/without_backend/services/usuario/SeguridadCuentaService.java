package com.without.without_backend.services.usuario;

import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.without.without_backend.dto.usuario.CambiarClaveRequest;
import com.without.without_backend.dto.usuario.CambiarCorreoRequest;
import com.without.without_backend.repositories.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class SeguridadCuentaService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    @Transactional
    public void cambiarClave(String correo, CambiarClaveRequest request) {
        var usuario = usuarios.findByCorreo(correo).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        if (!encoder.matches(request.actual(), usuario.getContraseña())) throw new IllegalArgumentException("La contraseña actual no es correcta");
        if (request.nueva().getBytes(StandardCharsets.UTF_8).length > 72) throw new IllegalArgumentException("La nueva contraseña no puede superar 72 bytes");
        if (encoder.matches(request.nueva(), usuario.getContraseña())) throw new IllegalArgumentException("La nueva contraseña debe ser diferente de la actual");
        usuario.setContraseña(encoder.encode(request.nueva()));
        usuarios.save(usuario);
    }

    /**
     * El usuario autenticado cambia su correo de acceso. Exige la contraseña actual (evita que alguien con la sesión
     * abierta se apropie de la cuenta). El correo es el «subject» del JWT: el front debe renovar el token después.
     */
    @Transactional
    public String cambiarCorreo(String correoActual, CambiarCorreoRequest request) {
        var usuario = usuarios.findByCorreo(correoActual).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        if (!encoder.matches(request.actual(), usuario.getContraseña())) throw new IllegalArgumentException("La contraseña actual no es correcta");
        String nuevo = request.correo().strip().toLowerCase();
        if (nuevo.equalsIgnoreCase(usuario.getCorreo())) throw new IllegalArgumentException("El nuevo correo debe ser diferente del actual");
        if (usuarios.findByCorreo(nuevo).isPresent()) throw new IllegalArgumentException("El correo ya está registrado");
        usuario.setCorreo(nuevo);
        usuarios.save(usuario);
        return nuevo;
    }
}
