package com.without.without_backend.repositories.cisterna;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.without.without_backend.entities.cisterna.Falla;

public interface FallaRepository extends JpaRepository<Falla, Integer>{
   @EntityGraph(attributePaths = {"cisterna", "tecnico"})
    List<Falla> findByCisterna_IdCisterna(Integer idCisterna);

    @Override
    @EntityGraph(attributePaths = {"cisterna", "tecnico"})
    List<Falla> findAll();  
}