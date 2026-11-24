package com.without.without_backend.dto.pedido.producto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

public record ProductoUpdateRequest(

    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    String nombre,

    @Size(max = 50, message = "El tipo de gas no puede superar los 50 caracteres")
    String tipoGas,

    @Size(max = 30, message = "La unidad de medida no puede superar los 30 caracteres")
    String unidadMedida,

    @DecimalMin(value = "0.01", message = "El precio unitario debe ser mayor a 0")
    BigDecimal precioUnitario

) {}