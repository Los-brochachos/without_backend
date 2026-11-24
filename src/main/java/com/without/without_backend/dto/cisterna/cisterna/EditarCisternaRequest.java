package com.without.without_backend.dto.cisterna.cisterna;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

public record EditarCisternaRequest(

        @Size(max = 100, message = "El nombre de búsqueda no puede superar los 100 caracteres")
        String nombreBuscar,

        @Size(max = 20, message = "La placa no puede superar los 20 caracteres")
        String placa,

        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @DecimalMin(value = "0.01", message = "La capacidad debe ser mayor a 0")
        BigDecimal capacidad

) {}