package com.without.without_backend.repositories.trabajador;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.without.without_backend.entities.trabajador.Conductor;
import com.without.without_backend.utils.Estado;

public interface ConductorRepository extends JpaRepository<Conductor, Integer> {
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Conductor c where c.trabajador.usuario.correo = :correo")
    Optional<Conductor> bloquearPorCorreo(@Param("correo") String correo);

    List<Conductor> findByTrabajadorEstado(Estado estado);

    Optional<Conductor> findByIdTrabajadorAndTrabajadorEstado(
            Integer idTrabajador,
            Estado estado
    );

    Optional<Conductor> findByLicenciaConducir(String licenciaConducir);

    Optional<Conductor> findByTrabajadorUsuarioCorreo(String correo);
}

