package com.without.without_backend.dto.cisterna.falla;

import java.time.LocalDateTime;

import com.without.without_backend.utils.cisterna.EstadoFalla;

public record FallaResponse(
    Integer idFalla,
    String descripcion,
    LocalDateTime fechaHora,
    EstadoFalla estado,
    Integer idCisterna,
    Integer idTrabajador
) {

}
