package com.without.without_backend.repositories.pedido;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.without.without_backend.entities.pedido.Producto;
import com.without.without_backend.utils.Estado;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    List<Producto> findByEstado(Estado estado);

    Optional<Producto> findByIdProductoAndEstado(
            Integer idProducto,
            Estado estado
    );

    Optional<Producto> findByNombre(String nombre);

    Optional<Producto> findByNombreAndEstado(
            String nombre,
            Estado estado
    );
}