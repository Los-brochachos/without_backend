package com.without.without_backend.controllers.usuario;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.without.without_backend.dto.usuario.CambiarClaveRequest;
import com.without.without_backend.services.usuario.SeguridadCuentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Cuenta", description = "Cambio de contraseña del usuario autenticado.")
@RestController @RequestMapping("/api/v1/cuenta") @RequiredArgsConstructor
public class SeguridadCuentaRestController {
    private final SeguridadCuentaService service;
    @PatchMapping("/clave")
    public ResponseEntity<Void> cambiarClave(Authentication authentication, @Valid @RequestBody CambiarClaveRequest request) {
        service.cambiarClave(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
