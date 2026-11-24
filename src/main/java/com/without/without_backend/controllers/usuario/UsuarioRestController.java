package com.without.without_backend.controllers.usuario;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.without.without_backend.dto.usuario.UsuarioResponse;
import com.without.without_backend.dto.usuario.UsuarioUpdateRequest;
import com.without.without_backend.services.usuario.SeguridadCuentaService;
import com.without.without_backend.services.usuario.EstadoCuentaService;
import com.without.without_backend.services.usuario.UsuarioService;
import com.without.without_backend.dto.usuario.CambiarCorreoRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Usuarios", description = "Cuentas de acceso al sistema y datos del usuario autenticado.")
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioRestController {

    private final UsuarioService service;
    private final SeguridadCuentaService seguridadCuenta;
    private final EstadoCuentaService estadoCuenta;

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> findById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me(
            Authentication authentication) {

        return ResponseEntity.ok(
                service.me(authentication.getName())
        );
    }

    /**
     * El usuario autenticado cambia su correo de acceso confirmando con su contraseña actual.
     * El correo es el «subject» del JWT: el token vigente deja de servir y el cliente debe renovarlo (refresh-token).
     */
    @PatchMapping ("/me")
    public ResponseEntity<UsuarioResponse> actualizarMe(
            Authentication authentication,
            @Valid @RequestBody CambiarCorreoRequest request) {

        String nuevo = seguridadCuenta.cambiarCorreo(authentication.getName(), request);
        return ResponseEntity.ok(service.me(nuevo));
    }

    /**
     * Cambio de EMERGENCIA del correo de otra cuenta (solo SUPERADMIN, ver SecurityConfig), p. ej. si un cliente
     * perdió el acceso a su correo. Cierra las sesiones abiertas de esa cuenta: deberá ingresar con el correo nuevo.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody UsuarioUpdateRequest request) {

        return ResponseEntity.ok(
                service.actualizar(id, request)
        );
    }

    /** Desactiva la cuenta (ADMIN). Si es de un trabajador aplica las reglas de Trabajadores; cierra sus sesiones. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(
            @PathVariable Integer id,
            Authentication authentication) {

        estadoCuenta.cambiarEstado(id, false, authentication.getName());

        return ResponseEntity.noContent().build();
    }

    /** Reactiva una cuenta desactivada (ADMIN). */
    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(
            @PathVariable Integer id,
            Authentication authentication) {

        estadoCuenta.cambiarEstado(id, true, authentication.getName());

        return ResponseEntity.noContent().build();
    }
}
