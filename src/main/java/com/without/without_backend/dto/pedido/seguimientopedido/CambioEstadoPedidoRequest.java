package com.without.without_backend.dto.pedido.seguimientopedido;

import com.without.without_backend.utils.pedido.EstadoPedido;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CambioEstadoPedidoRequest(

    @NotNull(message = "El estado es obligatorio")
    EstadoPedido estado,

    @Size(max = 250, message = "La observación no puede superar los 250 caracteres")
    String observacion

) {}