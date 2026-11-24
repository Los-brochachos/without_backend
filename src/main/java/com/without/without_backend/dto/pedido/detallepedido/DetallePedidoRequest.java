package com.without.without_backend.dto.pedido.detallepedido;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record DetallePedidoRequest(

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(
        value = "0.01",
        message = "La cantidad debe ser mayor a 0"
    )
    BigDecimal cantidad,

    @NotNull(message = "El producto es obligatorio")
    Integer idProducto

) {}