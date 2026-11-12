package com.without.without_backend.controllers.atencion;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.without.without_backend.dto.atencion.atencion.AtencionRequest;
import com.without.without_backend.dto.atencion.atencion.EditarAtencionRequest;
import com.without.without_backend.dto.atencion.atencion.AtencionResponse;
import com.without.without_backend.dto.atencion.atencion.CancelarAtencionRequest;
import com.without.without_backend.services.atencion.AtencionService;
import com.without.without_backend.utils.atencion.EstadoAtencion;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Atenciones", description = "Programación y ejecución de entregas (conductor + cisterna) asociadas a un pedido.")
@RequestMapping("/api/v1/atenciones")
@RequiredArgsConstructor
public class AtencionRestController {

    private final AtencionService service;

    @GetMapping("/asignadas")
    public ResponseEntity<List<AtencionResponse>> asignadas(org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(service.listarAsignadas(authentication.getName()));
    }

    @GetMapping("/asignadas/{id}")
    public ResponseEntity<AtencionResponse> asignada(@PathVariable Integer id, org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(service.buscarAsignada(id, authentication.getName()));
    }

    @GetMapping("/mis-atenciones")
    public ResponseEntity<List<AtencionResponse>> misAtenciones(
            org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(service.listarMisAtenciones(authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<AtencionResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtencionResponse> buscarPorId(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                service.buscarPorId(id)
        );
    }

    @GetMapping("/estado")
    public ResponseEntity<List<AtencionResponse>> listarPorEstado(
            @RequestParam EstadoAtencion estado) {

        return ResponseEntity.ok(
                service.listarPorEstado(estado)
        );
    }

    @GetMapping("/pedido/{idPedido}")
    public ResponseEntity<List<AtencionResponse>> listarPorPedido(
            @PathVariable Integer idPedido) {

        return ResponseEntity.ok(
                service.listarPorPedido(idPedido)
        );
    }

    @PostMapping
    public ResponseEntity<AtencionResponse> crear(
            @Valid @RequestBody AtencionRequest request,
            org.springframework.security.core.Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        service.crear(
                                request,
                                authentication.getName()
                        )
                );
    }

    @PatchMapping("/{id}/iniciar")
    public ResponseEntity<AtencionResponse> iniciar(
            @PathVariable Integer id, org.springframework.security.core.Authentication authentication) {

        return ResponseEntity.ok(
                service.iniciar(id, authentication.getName())
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AtencionResponse> editar(@PathVariable Integer id,
            @Valid @RequestBody EditarAtencionRequest request) {
        return ResponseEntity.ok(service.editar(id, request));
    }

    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<AtencionResponse> finalizar(
            @PathVariable Integer id, org.springframework.security.core.Authentication authentication) {

        return ResponseEntity.ok(
                service.finalizar(id, authentication.getName())
        );
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<AtencionResponse> cancelar(
            @PathVariable Integer id,
            @Valid @RequestBody CancelarAtencionRequest request) {

        return ResponseEntity.ok(
                service.cancelar(id, request)
        );
    }
}
