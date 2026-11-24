package com.without.without_backend.controllers.pedido;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.without.without_backend.dto.pedido.seguimientopedido.SeguimientoPedidoResponse;
import com.without.without_backend.services.pedido.SeguimientoPedidoService;

import lombok.RequiredArgsConstructor;


@RestController
@Tag(name = "Seguimiento de pedido", description = "Historial de estados de un pedido.")
@RequestMapping("/api/v1/pedidos/{idPedido}/seguimiento")
@RequiredArgsConstructor
public class SeguimientoPedidoRestController {

    private final SeguimientoPedidoService seguimientoPedidoService;

    @GetMapping("")
    public ResponseEntity<List<SeguimientoPedidoResponse>> listarPorPedidoCliente(
            Authentication authentication,
            @PathVariable Integer idPedido) {

        List<SeguimientoPedidoResponse> response =
                seguimientoPedidoService.listarPorPedidoCliente(idPedido, authentication.getName());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/todos")
    public ResponseEntity<List<SeguimientoPedidoResponse>> listarPorPedido(
            @PathVariable Integer idPedido) {

        List<SeguimientoPedidoResponse> response =
                seguimientoPedidoService.listarPorPedido(idPedido);

        return ResponseEntity.ok(response);
    }

    
}