package com.without.without_backend.controllers.cisterna;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.without.without_backend.dto.cisterna.falla.CambiarEstadoFallaRequest;
import com.without.without_backend.dto.cisterna.falla.FallaRequest;
import com.without.without_backend.dto.cisterna.falla.FallaResponse;
import com.without.without_backend.services.cisterna.FallaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Fallas", description = "Fallas de cisternas registradas por técnicos.")
@RequestMapping("/api/v1/fallas")
@RequiredArgsConstructor
public class FallaRestController {

    private final FallaService service;

    @GetMapping
    public ResponseEntity<List<FallaResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/cisterna/{idCisterna}")
    public ResponseEntity<List<FallaResponse>> listarPorCisterna(
            @PathVariable Integer idCisterna) {

        return ResponseEntity.ok(
                service.findByCisternaId(idCisterna)
        );
    }

    @PostMapping
    public ResponseEntity<FallaResponse> crear(
            @Valid @RequestBody FallaRequest request,
            Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.crear(
                        request,
                        authentication.getName()
                ));
    }

    @PatchMapping("/estado")
    public ResponseEntity<FallaResponse> cambiarEstado(
            @Valid @RequestBody CambiarEstadoFallaRequest request) {

        return ResponseEntity.ok(
                service.cambiarEstado(request)
        );
    }
}