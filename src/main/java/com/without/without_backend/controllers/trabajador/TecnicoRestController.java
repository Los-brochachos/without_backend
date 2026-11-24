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

import com.without.without_backend.dto.trabajador.tecnico.TecnicoCreateRequest;
import com.without.without_backend.dto.trabajador.tecnico.TecnicoResponse;
import com.without.without_backend.dto.trabajador.tecnico.TecnicoUpdateRequest;
import com.without.without_backend.services.trabajador.TecnicoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Técnicos", description = "Datos específicos de técnicos de mantenimiento.")
@RequestMapping("/api/v1/tecnicos")
@RequiredArgsConstructor
public class TecnicoRestController {

        private final TecnicoService tecnicoService;

        @GetMapping
        public ResponseEntity<List<TecnicoResponse>> listarTodos() {
                return ResponseEntity.ok(
                        tecnicoService.listarTodos()
                );
        }

        @GetMapping("/me")
        public ResponseEntity<TecnicoResponse> me(Authentication authentication) {

                return ResponseEntity.ok(
                        tecnicoService.me(authentication.getName())
                );
        }

        
        @GetMapping("/activos")
        public ResponseEntity<List<TecnicoResponse>> listarActivos() {
                return ResponseEntity.ok(
                        tecnicoService.listarActivos()
                );
        }

        @GetMapping("/{id}")
        public ResponseEntity<TecnicoResponse> buscarPorId(
                @PathVariable Integer id) {

                return ResponseEntity.ok(
                        tecnicoService.buscarPorId(id)
                );
        }

        @GetMapping("/{id}/activo")
        public ResponseEntity<TecnicoResponse> buscarActivoPorId(
                @PathVariable Integer id) {

                return ResponseEntity.ok(
                        tecnicoService.buscarActivoPorId(id)
                );
        }

        @PostMapping
        public ResponseEntity<TecnicoResponse> crear(
                @Valid @RequestBody TecnicoCreateRequest request) {

                TecnicoResponse response =
                        tecnicoService.crearTecnico(request);

                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(response);
        }

        @PutMapping("/{id}")
        public ResponseEntity<TecnicoResponse> actualizar(
                @PathVariable Integer id,
                @Valid @RequestBody TecnicoUpdateRequest request) {

                return ResponseEntity.ok(
                        tecnicoService.actualizarTecnico(
                                id,
                                request
                        )
                );
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> eliminar(
                @PathVariable Integer id) {

                tecnicoService.eliminarTecnico(id);

                return ResponseEntity.noContent().build();
        }
}
