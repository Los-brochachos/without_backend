package com.without.without_backend.mappers.pedido;

import org.springframework.stereotype.Component;

import com.without.without_backend.dto.pedido.pedido.PedidoRequest;
import com.without.without_backend.dto.pedido.pedido.PedidoResponse;
import com.without.without_backend.entities.cliente.Cliente;
import com.without.without_backend.entities.pedido.Pedido;

@Component
public class PedidoMapper {

    public Pedido toEntity(
            PedidoRequest request,
            Cliente cliente) {

        return Pedido.builder()
                .fechaEntregaEstimada(request.fechaEntregaEstimada())
                .prioridad(request.prioridad())
                .cliente(cliente)
                .build();
    }

    public PedidoResponse toResponse(Pedido pedido) {

        return new PedidoResponse(
                pedido.getIdPedido(),
                pedido.getFechaRegistro(),
                pedido.getEstado(),
                pedido.getFechaEntregaEstimada(),
                pedido.getPrioridad(),
                pedido.getCliente().getIdCliente(),
                pedido.getCliente().getRazonSocial()
        );
    }
}
