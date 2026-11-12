package com.without.without_backend.controllers.trabajador;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.without.without_backend.dto.trabajador.conductor.ConductorCreateRequest;
import com.without.without_backend.dto.trabajador.conductor.ConductorResponse;
import com.without.without_backend.dto.trabajador.conductor.ConductorUpdateRequest;
import com.without.without_backend.services.trabajador.ConductorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Conductores", description = "Datos específicos de conductores (licencia, etc.).")
@RequestMapping("/api/v1/conductores")
@RequiredArgsConstructor
public class ConductorRestController {

        private final ConductorService conductorService;

        @GetMapping
        public ResponseEntity<List<ConductorResponse>> listarTodos() {

                return ResponseEntity.ok(
                        conductorService.listarTodos()
                );
        }

        @GetMapping("/me")
        public ResponseEntity<ConductorResponse> me(
                Authentication authentication) {

                return ResponseEntity.ok(
                        conductorService.me(authentication.getName())
                );
        }



        @GetMapping("/activos")
        public ResponseEntity<List<ConductorResponse>> listarActivos() {

                return ResponseEntity.ok(
                        conductorService.listarActivos()
                );
        }


        @GetMapping("/{id}")
        public ResponseEntity<ConductorResponse> buscarPorId(
                @PathVariable Integer id) {

                return ResponseEntity.ok(
                        conductorService.buscarPorId(id)
                );
        }


        @GetMapping("/{id}/activo")
        public ResponseEntity<ConductorResponse> buscarActivoPorId(
                @PathVariable Integer id) {

                return ResponseEntity.ok(
                        conductorService.buscarActivoPorId(id)
                );
        }




        @PostMapping
        public ResponseEntity<ConductorResponse> crear(
                @Valid @RequestBody ConductorCreateRequest request) {

                ConductorResponse response =
                        conductorService.crearConductor(request);

                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(response);
        }

        @PutMapping("/{id}")
        public ResponseEntity<ConductorResponse> actualizar(
                @PathVariable Integer id,
                @Valid @RequestBody ConductorUpdateRequest request) {

                return ResponseEntity.ok(
                        conductorService.actualizarConductor(
                                id,
                                request
                        )
                );
        }


        // =========================
        // DESACTIVAR
        // =========================

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> eliminar(
                @PathVariable Integer id) {

                conductorService.eliminarConductor(id);

                return ResponseEntity.noContent().build();
        }
}
