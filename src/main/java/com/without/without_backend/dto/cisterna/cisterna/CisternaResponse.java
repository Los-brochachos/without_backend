package com.without.without_backend.dto.cisterna.cisterna;

import java.math.BigDecimal;

import com.without.without_backend.utils.Estado;

public record CisternaResponse(
    Integer idCisterna,
    String placa,
    String nombre,
    BigDecimal capacidad,
    Estado estado
){}