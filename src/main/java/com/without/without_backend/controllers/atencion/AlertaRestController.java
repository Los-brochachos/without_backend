package com.without.without_backend.controllers.atencion;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.without.without_backend.dto.atencion.alerta.AlertaRequest;
import com.without.without_backend.dto.atencion.alerta.AlertaResponse;
import com.without.without_backend.services.atencion.AlertaService;
import com.without.without_backend.utils.atencion.EstadoAlerta;
import com.without.without_backend.utils.atencion.TipoAlerta;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Alertas", description = "Incidencias reportadas por conductores durante una atención.")
@RequestMapping("/api/v1/alertas")
@RequiredArgsConstructor
public class AlertaRestController {

    private final AlertaService service;

    @GetMapping
    public ResponseEntity<List<AlertaResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertaResponse> buscarPorId(
            @PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @Operation(summary = "Mis alertas (CONDUCTOR)",
        description = "Solo las alertas de las atenciones asignadas al conductor autenticado (correo tomado del JWT).")
    @GetMapping("/mis-alertas")
    public ResponseEntity<List<AlertaResponse>> listarMisAlertas(
            @Parameter(hidden = true) Authentication authentication) {
        return ResponseEntity.ok(service.listarMisAlertas(authentication.getName()));
    }

    @Operation(summary = "Alertas de una atención",
        description = "ADMIN / ANALISTA / PROGRAMADOR: cualquier atención. CONDUCTOR: solo atenciones asignadas a él (si no, 404).")
    @GetMapping("/atencion/{idAtencion}")
    public ResponseEntity<List<AlertaResponse>> listarPorAtencion(
            @PathVariable Integer idAtencion,
            @Parameter(hidden = true) Authentication authentication) {
        boolean esConductor = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CONDUCTOR".equals(a.getAuthority()));
        return ResponseEntity.ok(esConductor
                ? service.listarPorAtencionDelConductor(idAtencion, authentication.getName())
                : service.listarPorAtencion(idAtencion));
    }

    @GetMapping("/estado")
    public ResponseEntity<List<AlertaResponse>> listarPorEstado(
            @RequestParam EstadoAlerta estado) {
        return ResponseEntity.ok(
                service.listarPorEstado(estado)
        );
    }

    @GetMapping("/tipo")
    public ResponseEntity<List<AlertaResponse>> listarPorTipo(
            @RequestParam TipoAlerta tipo) {
        return ResponseEntity.ok(
                service.listarPorTipo(tipo)
        );
    }

    @PostMapping
    public ResponseEntity<AlertaResponse> crear(
            @Valid @RequestBody AlertaRequest request,
            Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        service.crear(
                                request,
                                authentication.getName()
                        )
                );
    }

    @PatchMapping("/{id}/revisar")
    public ResponseEntity<AlertaResponse> marcarRevisada(
            @PathVariable Integer id) {
        return ResponseEntity.ok(
                service.marcarRevisada(id)
        );
    }

    @PatchMapping("/{id}/atender")
    public ResponseEntity<AlertaResponse> marcarAtendida(
            @PathVariable Integer id) {
        return ResponseEntity.ok(
                service.marcarAtendida(id)
        );
    }
}