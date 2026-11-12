package com.without.without_backend.repositories.atencion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.without.without_backend.entities.atencion.Alerta;
import com.without.without_backend.utils.atencion.EstadoAlerta;
import com.without.without_backend.utils.atencion.TipoAlerta;

public interface AlertaRepository
        extends JpaRepository<Alerta, Integer> {

    List<Alerta> findByAtencionIdAtencion(
            Integer idAtencion
    );

    List<Alerta> findByEstado(
            EstadoAlerta estado
    );

    List<Alerta> findByTipo(
            TipoAlerta tipo
    );

    List<Alerta> findByAtencionIdAtencionAndEstado(
            Integer idAtencion,
            EstadoAlerta estado
    );

    /** Alertas de las atenciones asignadas a un conductor (por el correo de su usuario), más recientes primero. */
    List<Alerta> findByAtencionConductorTrabajadorUsuarioCorreoOrderByFechaHoraDesc(String correo);
}