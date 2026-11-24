package com.without.without_backend.controllers.trabajador;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.without.without_backend.dto.trabajador.TrabajadorCreateRequest;
import com.without.without_backend.dto.trabajador.TrabajadorResponse;
import com.without.without_backend.services.trabajador.TrabajadorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Trabajadores", description = "Gestión del personal y datos del trabajador autenticado.")
@RequestMapping("/api/v1/trabajadores")
@RequiredArgsConstructor
public class TrabajadorRestController {

    private final TrabajadorService trabajadorService;

    @PutMapping("/{id}")
    public ResponseEntity<TrabajadorResponse> actualizar(@PathVariable Integer id,
            @Valid @RequestBody com.without.without_backend.dto.trabajador.TrabajadorUpdateRequest request) {
        return ResponseEntity.ok(trabajadorService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TrabajadorResponse> desactivar(@PathVariable Integer id,
            org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(trabajadorService.cambiarActivo(id, false, authentication.getName()));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<TrabajadorResponse> activar(@PathVariable Integer id,
            org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(trabajadorService.cambiarActivo(id, true, authentication.getName()));
    }

    @GetMapping("/me")
    public ResponseEntity<TrabajadorResponse> me(org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(trabajadorService.me(authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<TrabajadorResponse>> listarTodos() {

        return ResponseEntity.ok(
                trabajadorService.listarTodos()
        );
    }

    @GetMapping("/activos")
    public ResponseEntity<List<TrabajadorResponse>> listarActivos() {

        return ResponseEntity.ok(
                trabajadorService.listarActivos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrabajadorResponse> buscarPorId(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                trabajadorService.buscarPorId(id)
        );
    }

    @GetMapping("/{id}/activo")
    public ResponseEntity<TrabajadorResponse> buscarActivoPorId(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                trabajadorService.buscarActivoPorId(id)
        );
    }

    @GetMapping("/dni/{dni}")
    public ResponseEntity<TrabajadorResponse> buscarPorDni(
            @PathVariable String dni) {

        return ResponseEntity.ok(
                trabajadorService.buscarPorDni(dni)
        );
    }

    @GetMapping("/dni/{dni}/activo")
    public ResponseEntity<TrabajadorResponse> buscarActivoPorDni(
            @PathVariable String dni) {

        return ResponseEntity.ok(
                trabajadorService.buscarActivoPorDni(dni)
        );
    }

    @PostMapping
    public ResponseEntity<TrabajadorResponse> crear(
        @Valid @RequestBody TrabajadorCreateRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(trabajadorService.crear(request));
    }
}
