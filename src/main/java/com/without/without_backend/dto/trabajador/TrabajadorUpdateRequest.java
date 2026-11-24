package com.without.without_backend.dto.trabajador;

import java.time.LocalDate;
import jakarta.validation.constraints.*;

/** Datos comunes. El rol y las credenciales no se modifican en esta operación. */
public record TrabajadorUpdateRequest(
    @NotBlank @Size(max = 100) String nombres,
    @NotBlank @Size(max = 100) String apellidos,
    @NotBlank @Pattern(regexp = "\\d{8}", message = "El DNI debe contener exactamente 8 dígitos") String dni,
    @Pattern(regexp = "(?:[1-8]\\d{7}|9\\d{8})?", message = "Ingresa un teléfono fijo de 8 dígitos o celular de 9 dígitos") String telefono,
    @Size(max = 150) String direccion,
    @NotNull LocalDate fechaIngreso
) {}
