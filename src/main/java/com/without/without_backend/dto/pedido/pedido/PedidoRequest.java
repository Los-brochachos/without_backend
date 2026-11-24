package com.without.without_backend.dto.pedido.pedido;

import java.time.LocalDate;
import java.util.List;

import com.without.without_backend.dto.pedido.detallepedido.DetallePedidoRequest;
import com.without.without_backend.utils.pedido.PrioridadPedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PedidoRequest(

    @NotNull(message = "La fecha de entrega estimada es obligatoria")
    LocalDate fechaEntregaEstimada,

    @NotNull(message = "La prioridad es obligatoria")
    PrioridadPedido prioridad,

    @NotEmpty(message = "El pedido debe contener al menos un producto")
    @Valid
    List<DetallePedidoRequest> detalles

) {}