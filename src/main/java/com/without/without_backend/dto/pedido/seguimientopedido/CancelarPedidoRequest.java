package com.without.without_backend.dto.pedido.seguimientopedido;

import jakarta.validation.constraints.Size;

public record CancelarPedidoRequest(

    @Size(
        max = 250,
        message = "La observación no puede superar los 250 caracteres"
    )
    String observacion

) {}