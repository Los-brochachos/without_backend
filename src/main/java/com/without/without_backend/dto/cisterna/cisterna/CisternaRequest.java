package com.without.without_backend.dto.cisterna.cisterna;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CisternaRequest(
    @NotBlank @Size (max = 20, message = "Límite de caracteres excedido") String placa,
    @NotBlank @Size (max=100, message = "Límite de caracteres excedido") String nombre,
    @NotNull @Positive @Digits (integer = 5, fraction = 2, message = "Capacidad inválida") BigDecimal capacidad
){}
