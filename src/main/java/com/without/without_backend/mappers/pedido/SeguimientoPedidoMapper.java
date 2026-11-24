package com.without.without_backend.mappers.pedido;

import org.springframework.stereotype.Component;

import com.without.without_backend.dto.pedido.seguimientopedido.SeguimientoPedidoResponse;
import com.without.without_backend.entities.pedido.SeguimientoPedido;

@Component
public class SeguimientoPedidoMapper {

    public SeguimientoPedidoResponse toResponse(
            SeguimientoPedido seguimiento) {

        return new SeguimientoPedidoResponse(
                seguimiento.getIdSeguimiento(),
                seguimiento.getFechaHora(),
                seguimiento.getEstado(),
                seguimiento.getObservacion(),
                seguimiento.getPedido().getIdPedido()
        );
    }
}