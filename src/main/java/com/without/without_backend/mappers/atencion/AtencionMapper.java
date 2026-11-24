package com.without.without_backend.mappers.atencion;

import org.springframework.stereotype.Component;

import com.without.without_backend.dto.atencion.atencion.AtencionRequest;
import com.without.without_backend.dto.atencion.atencion.AtencionResponse;
import com.without.without_backend.entities.atencion.Atencion;
import com.without.without_backend.entities.cisterna.Cisterna;
import com.without.without_backend.entities.pedido.Pedido;
import com.without.without_backend.entities.trabajador.Conductor;
import com.without.without_backend.utils.atencion.EstadoAtencion;

@Component
public class AtencionMapper {

    public Atencion toEntity(AtencionRequest dto) {

        if (dto == null) {
            return null;
        }

        Pedido pedido = Pedido.builder()
                .idPedido(dto.idPedido())
                .build();

        Conductor conductor = Conductor.builder()
                .idTrabajador(dto.idConductor())
                .build();

        Cisterna cisterna = Cisterna.builder()
                .idCisterna(dto.idCisterna())
                .build();

        return Atencion.builder()
                .fechaInicioProgramada(dto.fechaInicioProgramada())
                .fechaFinProgramada(dto.fechaFinProgramada())
                .observaciones(dto.observaciones())
                .pedido(pedido)
                .conductor(conductor)
                .cisterna(cisterna)
                .estado(EstadoAtencion.PROGRAMADA)
                .build();
    }

    public AtencionResponse toDto(Atencion entity) {

        if (entity == null) {
            return null;
        }

        return new AtencionResponse(
                entity.getIdAtencion(),
                entity.getFechaInicioProgramada(),
                entity.getFechaFinProgramada(),
                entity.getFechaInicio(),
                entity.getFechaFin(),
                entity.getEstado(),
                entity.getObservaciones(),
                entity.getPedido() != null
                        ? entity.getPedido().getIdPedido()
                        : null,
                entity.getConductor() != null
                        ? entity.getConductor().getIdTrabajador()
                        : null,
                entity.getCisterna() != null
                        ? entity.getCisterna().getIdCisterna()
                        : null,
                entity.getProgramador() != null
                        ? entity.getProgramador().getIdTrabajador()
                        : null,
                entity.getPedido() != null && entity.getPedido().getCliente() != null
                        ? entity.getPedido().getCliente().getRazonSocial() : null,
                entity.getConductor() != null && entity.getConductor().getTrabajador() != null
                        ? java.util.stream.Stream.of(entity.getConductor().getTrabajador().getNombres(),
                                entity.getConductor().getTrabajador().getApellidos())
                                .filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.joining(" ")) : null,
                entity.getCisterna() != null
                        ? entity.getCisterna().getPlaca() + " · " + entity.getCisterna().getNombre() : null
        );
    }
}
