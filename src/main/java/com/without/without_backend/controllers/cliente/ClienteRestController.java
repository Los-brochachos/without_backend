package com.without.without_backend.controllers.cliente;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.without.without_backend.dto.cliente.ClienteCreateRequest;
import com.without.without_backend.dto.cliente.ClienteResponse;
import com.without.without_backend.dto.cliente.ClienteUpdateRequest;
import com.without.without_backend.services.cliente.ClienteService;
import com.without.without_backend.utils.auth.RegisterResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;



@RestController
@Tag(name = "Clientes", description = "Registro público de clientes y gestión de empresas cliente.")
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteRestController {

    private final ClienteService clienteS;
    
    // Listar todos los clientes
    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar() {

        return ResponseEntity.ok(
                clienteS.listarClientesTodos()
        );
    }

    // Listar solo clientes activos
    @GetMapping("/activos")
    public ResponseEntity<List<ClienteResponse>> listarActivos() {
        return ResponseEntity.ok(
            clienteS.listarActivos()
        );
    }


    @GetMapping("/me")
    public ResponseEntity<ClienteResponse> me(
            Authentication authentication) {

        return ResponseEntity.ok(
                clienteS.me(authentication.getName())
        );
    }

    @PatchMapping("/me")
    public ResponseEntity<ClienteResponse> actualizarMe(
            Authentication authentication,
            @Valid @RequestBody ClienteUpdateRequest request) {

        return ResponseEntity.ok(
                clienteS.actualizarMe(
                        authentication.getName(),
                        request
                )
        );
    }
        
    // Buscar cliente por ID, sin importar su estado
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscarCliente(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                clienteS.buscarPorId(id)
        );
    }


    @GetMapping("/{id}/activo")
    public ResponseEntity<ClienteResponse> buscarActivoPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(
            clienteS.buscarActivoPorId(id)
        );
    }
    

    @Operation(summary = "Registrar cliente (público)",
        description = "Crea la empresa cliente y su usuario con rol CLIENTE. No requiere token.")
    @SecurityRequirements
    @ApiResponse(responseCode = "400", description = "Datos inválidos, RUC o correo ya registrados")
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> crearCliente(
            @Valid @RequestBody ClienteCreateRequest request) {

        RegisterResponse cliente =
                clienteS.crearCliente(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cliente);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizarCliente(
            @PathVariable Integer id,
            @Valid @RequestBody ClienteUpdateRequest request) {

        ClienteResponse cliente =
                clienteS.actualizarCliente(id, request);

        return ResponseEntity.ok(cliente);
    }

    

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(
            @PathVariable Integer id) {

        clienteS.eliminarCliente(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}