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
import com.without.without_backend.services.usuario.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Usuarios", description = "Cuentas de acceso al sistema y datos del usuario autenticado.")
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioRestController {

    private final UsuarioService service;

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

    @PatchMapping ("/me")
    public ResponseEntity<UsuarioResponse> actualizarMe(
            Authentication authentication,
            @Valid @RequestBody UsuarioUpdateRequest request) {

        return ResponseEntity.ok(
                service.actualizarMe(authentication.getName(), request)
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody UsuarioUpdateRequest request) {

        return ResponseEntity.ok(
                service.actualizar(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(
            @PathVariable Integer id) {

        service.desactivar(id);

        return ResponseEntity.noContent().build();
    }
}
