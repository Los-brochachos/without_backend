package com.without.without_backend.dto.pedido.pedido;

import java.time.LocalDate;

import com.without.without_backend.utils.pedido.EstadoPedido;
import com.without.without_backend.utils.pedido.PrioridadPedido;

public record PedidoResponse(

    Integer idPedido,
    LocalDate fechaRegistro,
    EstadoPedido estado,
    LocalDate fechaEntregaEstimada,
    PrioridadPedido prioridad,
    Integer idCliente,
    String razonSocial

) {}
