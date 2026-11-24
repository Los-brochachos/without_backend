package com.without.without_backend.dto.pedido.detallepedido;

import java.math.BigDecimal;

import com.without.without_backend.utils.Estado;

public record DetallePedidoResponse(

    Integer idDetalle,
    BigDecimal cantidad,
    BigDecimal precioUnitario,
    Estado estado,
    Integer idProducto,
    String nombreProducto

) {}