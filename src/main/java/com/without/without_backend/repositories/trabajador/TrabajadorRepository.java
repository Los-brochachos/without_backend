package com.without.without_backend.repositories.trabajador;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.without.without_backend.entities.trabajador.Trabajador;
import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.utils.Estado;

public interface TrabajadorRepository extends JpaRepository<Trabajador, Integer> {
    @org.springframework.data.jpa.repository.Query("select count(a) from Atencion a where a.conductor.idTrabajador = :id and a.estado in :estados")
    long contarAtencionesActivas(@org.springframework.data.repository.query.Param("id") Integer id,
        @org.springframework.data.repository.query.Param("estados") java.util.Collection<com.without.without_backend.utils.atencion.EstadoAtencion> estados);

    Optional<Trabajador> findByUsuario_Correo(String correo);

    List<Trabajador> findByEstado(Estado estado);

    Optional<Trabajador> findByIdTrabajadorAndEstado(
            Integer idTrabajador,
            Estado estado
    );

    Optional<Trabajador> findByDni(String dni);

    Optional<Trabajador> findByUsuario(Usuario usuario);

    Optional<Trabajador> findByDniAndEstado(
        String dni,
        Estado estado
);
}
