package com.without.without_backend.repositories.pedido;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.without.without_backend.entities.pedido.DetallePedido;
import com.without.without_backend.utils.Estado;
import org.springframework.data.jpa.repository.Query;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Integer> {

        @Query("SELECT d FROM DetallePedido d WHERE d.pedido.idPedido = :idPedido")
        List<DetallePedido> findByPedidoIdPedido(Integer idPedido);

        List<DetallePedido> findByPedidoIdPedidoAndEstado(
                Integer idPedido,
                Estado estado);

        long countByPedidoIdPedidoAndEstado(Integer idPedido, Estado estado);

        boolean existsByPedidoIdPedidoAndProductoIdProductoAndEstado(
                Integer idPedido,
                Integer idProducto,
                Estado estado);
}