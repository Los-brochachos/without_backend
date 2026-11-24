package com.without.without_backend.controllers.cisterna;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.without.without_backend.dto.cisterna.cisterna.CisternaRequest;
import com.without.without_backend.dto.cisterna.cisterna.CisternaResponse;
import com.without.without_backend.dto.cisterna.cisterna.EditarCisternaRequest;
import com.without.without_backend.dto.cisterna.cisterna.EliminarCisternaRequest;
import com.without.without_backend.services.cisterna.CisternaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Cisternas", description = "Flota de cisternas de transporte.")
@RequestMapping("/api/v1/cisternas")
@RequiredArgsConstructor
public class CisternaRestController {

    private final CisternaService service;

    // LISTAR TODAS
    @GetMapping
    public ResponseEntity<List<CisternaResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    // LISTAR ACTIVAS
    @GetMapping("/activas")
    public ResponseEntity<List<CisternaResponse>> listarActivas() {
        return ResponseEntity.ok(service.listarActivas());
    }

    // BUSCAR POR ID
    @GetMapping("/{id}")
    public ResponseEntity<CisternaResponse> buscarPorId(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                service.buscarPorId(id)
        );
    }

    // BUSCAR ACTIVA POR ID
    @GetMapping("/{id}/activa")
    public ResponseEntity<CisternaResponse> buscarActivaPorId(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                service.buscarActivaPorId(id)
        );
    }

    // BUSCAR POR PLACA
    @GetMapping("/placa/{placa}")
    public ResponseEntity<CisternaResponse> buscarPorPlaca(
            @PathVariable String placa) {

        return ResponseEntity.ok(
                service.buscarPorPlaca(placa)
        );
    }

    // BUSCAR POR NOMBRE
    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<CisternaResponse> buscarPorNombre(
            @PathVariable String nombre) {

        return ResponseEntity.ok(
                service.buscarPorNombre(nombre)
        );
    }

    // CREAR
    @PostMapping
    public ResponseEntity<CisternaResponse> crear(
            @Valid @RequestBody CisternaRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.crear(request));
    }

    // EDITAR
    @PutMapping
    public ResponseEntity<CisternaResponse> editar(
            @Valid @RequestBody EditarCisternaRequest request) {

        return ResponseEntity.ok(
                service.editar(request)
        );
    }

    // DESACTIVAR
    @DeleteMapping
    public ResponseEntity<CisternaResponse> eliminar(
            @Valid @RequestBody EliminarCisternaRequest request) {

        return ResponseEntity.ok(
                service.eliminar(request)
        );
    }
}