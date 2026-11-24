package com.without.without_backend.repositories.atencion;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.without.without_backend.entities.atencion.Atencion;
import com.without.without_backend.utils.atencion.EstadoAtencion;

public interface AtencionRepository extends JpaRepository<Atencion, Integer> {
    boolean existsByConductor_IdTrabajadorAndEstado(Integer idConductor, EstadoAtencion estado);

    List<Atencion> findByConductor_Trabajador_Usuario_Correo(String correo);

    Optional<Atencion> findByIdAtencionAndConductor_Trabajador_Usuario_Correo(Integer id, String correo);

    List<Atencion> findByPedido_Cliente_Usuario_Correo(String correo);

    List<Atencion> findByEstado(EstadoAtencion estado);

    List<Atencion> findByPedidoIdPedido(Integer idPedido);

    Optional<Atencion> findByIdAtencionAndEstado(
            Integer idAtencion,
            EstadoAtencion estado
    );

    List<Atencion> findByConductor_IdTrabajador(Integer idTrabajador);

    List<Atencion> findByCisterna_IdCisterna(Integer idCisterna);

    List<Atencion> findByProgramador_IdTrabajador(Integer idTrabajador);
}
