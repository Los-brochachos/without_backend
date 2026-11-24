package com.without.without_backend.dto.pedido.producto;

import java.math.BigDecimal;

import com.without.without_backend.utils.Estado;

public record ProductoResponse(
    Integer idProducto,
    String nombre,
    String tipoGas,
    String unidadMedida,
    BigDecimal precioUnitario,
    Estado estado
) {}