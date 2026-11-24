package com.without.without_backend.mappers.pedido;

import org.springframework.stereotype.Component;

import com.without.without_backend.dto.pedido.detallepedido.DetallePedidoRequest;
import com.without.without_backend.dto.pedido.detallepedido.DetallePedidoResponse;
import com.without.without_backend.entities.pedido.DetallePedido;
import com.without.without_backend.entities.pedido.Pedido;
import com.without.without_backend.entities.pedido.Producto;

@Component
public class DetallePedidoMapper {

    public DetallePedido toEntity(
            DetallePedidoRequest request,
            Pedido pedido,
            Producto producto) {

        return DetallePedido.builder()
                .cantidad(request.cantidad())
                .precioUnitario(producto.getPrecioUnitario())
                .pedido(pedido)
                .producto(producto)
                .build();
    }

    public DetallePedidoResponse toResponse(DetallePedido detalle) {

        return new DetallePedidoResponse(
                detalle.getIdDetalle(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getEstado(),
                detalle.getProducto().getIdProducto(),
                detalle.getProducto().getNombre()
        );
    }
}