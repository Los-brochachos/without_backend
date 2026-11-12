package com.without.without_backend.controllers.pedido;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.without.without_backend.dto.pedido.detallepedido.AgregarCantidadDetalleRequest;
import com.without.without_backend.dto.pedido.detallepedido.DetallePedidoRequest;
import com.without.without_backend.dto.pedido.detallepedido.DetallePedidoResponse;
import com.without.without_backend.services.pedido.DetallePedidoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Detalle de pedido", description = "Productos y cantidades de cada pedido.")
@RequestMapping("/api/v1/pedidos/{idPedido}/detalles")
@RequiredArgsConstructor
public class DetallePedidoRestController {

    private final DetallePedidoService detallePedidoService;

    // CLIENTE: listar detalles activos de su propio pedido
    @GetMapping("")
    public ResponseEntity<List<DetallePedidoResponse>> listarActivosPorPedidoCliente(
            @PathVariable Integer idPedido,
            Authentication authentication) {

        return ResponseEntity.ok(
                detallePedidoService.listarActivosPorPedidoCliente(
                        idPedido,
                        authentication.getName()
                )
        );
    }

    // ADMIN / ANALISTA / PROGRAMADOR:
    // listar detalles activos de cualquier pedido
    @GetMapping("/activos")
    public ResponseEntity<List<DetallePedidoResponse>> listarActivosPorPedido(
            @PathVariable Integer idPedido) {

        return ResponseEntity.ok(
                detallePedidoService.listarActivosPorPedido(idPedido)
        );
    }

    // ADMIN / ANALISTA / PROGRAMADOR:
    // listar todos los detalles, incluidos los inactivos
    @GetMapping("/todos")
    public ResponseEntity<List<DetallePedidoResponse>> listarTodosPorPedido(
            @PathVariable Integer idPedido) {

        return ResponseEntity.ok(
                detallePedidoService.listarTodosPorPedido(idPedido)
        );
    }

    // CLIENTE: agregar detalle a su propio pedido
    @PostMapping("")
    public ResponseEntity<DetallePedidoResponse> agregarDetalle(
            @PathVariable Integer idPedido,
            @Valid @RequestBody DetallePedidoRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                detallePedidoService.agregarDetalleCliente(
                        idPedido,
                        request,
                        authentication.getName()
                )
        );
    }

    // CLIENTE: aumentar cantidad de un detalle de su propio pedido
    @PatchMapping("/{idDetalle}/cantidad")
    public ResponseEntity<DetallePedidoResponse> agregarCantidad(
            @PathVariable Integer idPedido,
            @PathVariable Integer idDetalle,
            @Valid @RequestBody AgregarCantidadDetalleRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                detallePedidoService.agregarCantidadCliente(
                        idPedido,
                        idDetalle,
                        request,
                        authentication.getName()
                )
        );
    }

    // CLIENTE: cancelar un detalle de su propio pedido
    @PatchMapping("/{idDetalle}/cancelar")
    public ResponseEntity<DetallePedidoResponse> cancelar(
            @PathVariable Integer idPedido,
            @PathVariable Integer idDetalle,
            Authentication authentication) {

        return ResponseEntity.ok(
                detallePedidoService.cancelarCliente(
                        idPedido,
                        idDetalle,
                        authentication.getName()
                )
        );
    }
}

