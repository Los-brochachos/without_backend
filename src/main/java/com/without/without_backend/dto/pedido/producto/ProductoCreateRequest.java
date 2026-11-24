package com.without.without_backend.dto.pedido.producto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductoCreateRequest(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    String nombre,

    @NotBlank(message = "El tipo de gas es obligatorio")
    @Size(max = 50, message = "El tipo de gas no puede superar los 50 caracteres")
    String tipoGas,

    @NotBlank(message = "La unidad de medida es obligatoria")
    @Size(max = 30, message = "La unidad de medida no puede superar los 30 caracteres")
    String unidadMedida,

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false,
            message = "El precio debe ser mayor que 0")
    BigDecimal precioUnitario

) {}