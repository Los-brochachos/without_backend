package com.without.without_backend.repositories.pedido;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.without.without_backend.entities.pedido.SeguimientoPedido;

public interface SeguimientoPedidoRepository
        extends JpaRepository<SeguimientoPedido, Integer> {

    List<SeguimientoPedido> findByPedidoIdPedidoOrderByFechaHoraAsc(
            Integer idPedido
    );

}
