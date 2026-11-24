package com.without.without_backend.dto.atencion.alerta;

import com.without.without_backend.utils.atencion.TipoAlerta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AlertaRequest(

        @NotNull(message = "El tipo de alerta es obligatorio")
        TipoAlerta tipo,

        @NotBlank(message = "El mensaje es obligatorio")
        @Size(
                max = 250,
                message = "El mensaje no puede superar los 250 caracteres"
        )
        String mensaje,

        @NotNull(message = "La atención es obligatoria")
        Integer idAtencion

) {}