package com.without.without_backend.dto.trabajador.tecnico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TecnicoRequest(

    @NotBlank(message = "La especialidad es obligatoria")
    @Size(max = 100, message = "La especialidad no puede superar los 100 caracteres")
    String especialidad,

    @NotBlank(message = "El nivel técnico es obligatorio")
    @Size(max = 50, message = "El nivel técnico no puede superar los 50 caracteres")
    String nivelTecnico,

    @NotBlank(message = "La certificación es obligatoria")
    @Size(max = 100, message = "La certificación no puede superar los 100 caracteres")
    String certificacion

) {}