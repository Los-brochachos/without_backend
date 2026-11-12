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

import com.without.without_backend.dto.trabajador.programador.ProgramadorCreateRequest;
import com.without.without_backend.dto.trabajador.programador.ProgramadorResponse;
import com.without.without_backend.dto.trabajador.programador.ProgramadorUpdateRequest;
import com.without.without_backend.services.trabajador.ProgramadorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Programadores", description = "Datos específicos de programadores.")
@RequestMapping("/api/v1/programadores")
@RequiredArgsConstructor 
public class ProgramadorRestController {

        private final ProgramadorService programadorService;


        @GetMapping
        public ResponseEntity<List<ProgramadorResponse>> listarTodos() {
                return ResponseEntity.ok(
                        programadorService.listarTodos()
                );
        }

        @GetMapping("/me")
                public ResponseEntity<ProgramadorResponse> me(
                        Authentication authentication) {

                return ResponseEntity.ok(
                        programadorService.me(authentication.getName())
                );
        }


        @GetMapping("/activos")
        public ResponseEntity<List<ProgramadorResponse>> listarActivos() {
                return ResponseEntity.ok(
                        programadorService.listarActivos()
                );
        }

        @GetMapping("/{id}")
        public ResponseEntity<ProgramadorResponse> buscarPorId(
                @PathVariable Integer id) {

                return ResponseEntity.ok(
                        programadorService.buscarPorId(id)
                );
        }

        @GetMapping("/{id}/activo")
        public ResponseEntity<ProgramadorResponse> buscarActivoPorId(
                @PathVariable Integer id) {

                return ResponseEntity.ok(
                        programadorService.buscarActivoPorId(id)
                );
        }

        @PostMapping
        public ResponseEntity<ProgramadorResponse> crear(
                @Valid @RequestBody ProgramadorCreateRequest request) {

                ProgramadorResponse response =
                        programadorService.crearProgramador(request);

                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(response);
        }

        @PutMapping("/{id}")
        public ResponseEntity<ProgramadorResponse> actualizar(
                @PathVariable Integer id,
                @Valid @RequestBody ProgramadorUpdateRequest request) {

                return ResponseEntity.ok(
                        programadorService.actualizarProgramador(
                                id,
                                request
                        )
                );
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> eliminar(
                @PathVariable Integer id) {

                programadorService.eliminarProgramador(id);

                return ResponseEntity.noContent().build();
        }
}