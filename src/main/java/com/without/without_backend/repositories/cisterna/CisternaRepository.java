package com.without.without_backend.repositories.cisterna;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.without.without_backend.entities.cisterna.Cisterna;
import com.without.without_backend.utils.Estado;

public interface CisternaRepository extends JpaRepository<Cisterna, Integer> {
    Optional<Cisterna> findByPlaca(String placa);
    Optional<Cisterna> findByNombre(String nombre);

    List<Cisterna> findByEstado(Estado estado);

    Optional<Cisterna> findByIdCisternaAndEstado(
            Integer idCisterna,
            Estado estado
    );

    boolean existsByPlaca(String placa);

    boolean existsByNombre(String nombre);
}

