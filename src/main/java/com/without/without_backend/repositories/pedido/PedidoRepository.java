package com.without.without_backend.repositories.pedido;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.without.without_backend.entities.pedido.Pedido;
import com.without.without_backend.utils.pedido.EstadoPedido;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    @Override
    @EntityGraph(attributePaths = "cliente")
    List<Pedido> findAll();

    List<Pedido> findByClienteIdCliente(Integer idCliente);

    // ===== Paginación (LIMIT/OFFSET y COUNT los genera Spring Data) =====
    @Override
    @EntityGraph(attributePaths = "cliente")
    Page<Pedido> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "cliente")
    Page<Pedido> findByEstado(EstadoPedido estado, Pageable pageable);

    @EntityGraph(attributePaths = "cliente")
    Page<Pedido> findByClienteIdCliente(Integer idCliente, Pageable pageable);

    @EntityGraph(attributePaths = "cliente")
    Page<Pedido> findByClienteIdClienteAndEstado(Integer idCliente, EstadoPedido estado, Pageable pageable);

    Optional<Pedido> findByIdPedidoAndClienteIdCliente(
            Integer idPedido,
            Integer idCliente
    );
}
