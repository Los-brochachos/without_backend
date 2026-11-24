package com.without.without_backend.services.atencion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.without.without_backend.dto.atencion.atencion.AtencionRequest;
import com.without.without_backend.dto.atencion.atencion.EditarAtencionRequest;
import com.without.without_backend.dto.atencion.atencion.AtencionResponse;
import com.without.without_backend.dto.atencion.atencion.CancelarAtencionRequest;
import com.without.without_backend.entities.atencion.Atencion;
import com.without.without_backend.entities.cisterna.Cisterna;
import com.without.without_backend.entities.pedido.Pedido;
import com.without.without_backend.entities.pedido.SeguimientoPedido;
import com.without.without_backend.repositories.pedido.SeguimientoPedidoRepository;
import com.without.without_backend.entities.trabajador.Conductor;
import com.without.without_backend.entities.trabajador.Programador;
import com.without.without_backend.mappers.atencion.AtencionMapper;
import com.without.without_backend.repositories.atencion.AtencionRepository;
import com.without.without_backend.repositories.cisterna.CisternaRepository;
import com.without.without_backend.repositories.pedido.PedidoRepository;
import com.without.without_backend.repositories.trabajador.ConductorRepository;
import com.without.without_backend.repositories.trabajador.ProgramadorRepository;
import com.without.without_backend.utils.Estado;
import com.without.without_backend.utils.atencion.EstadoAtencion;
import com.without.without_backend.utils.pedido.EstadoPedido;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AtencionService {

        private final AtencionRepository repository;
        private final PedidoRepository pedidoRepository;
        private final ConductorRepository conductorRepository;
        private final CisternaRepository cisternaRepository;
        private final ProgramadorRepository programadorRepository;
        private final AtencionMapper mapper;
        private final SeguimientoPedidoRepository seguimientos;

        @org.springframework.transaction.annotation.Transactional(readOnly = true)
        public List<AtencionResponse> listarAsignadas(String correo) {
                return repository.findByConductor_Trabajador_Usuario_Correo(correo)
                        .stream().map(mapper::toDto).toList();
        }

        @org.springframework.transaction.annotation.Transactional(readOnly = true)
        public AtencionResponse buscarAsignada(Integer id, String correo) {
                return mapper.toDto(asignada(id, correo));
        }

        private Atencion asignada(Integer id, String correo) {
                return repository.findByIdAtencionAndConductor_Trabajador_Usuario_Correo(id, correo)
                        .orElseThrow(() -> new NoSuchElementException("La atención no existe o no está asignada a tu cuenta"));
        }

        @org.springframework.transaction.annotation.Transactional(readOnly = true)
        public List<AtencionResponse> listarMisAtenciones(String correo) {
                return repository.findByPedido_Cliente_Usuario_Correo(correo)
                        .stream().map(mapper::toDto).toList();
        }

        @org.springframework.transaction.annotation.Transactional(readOnly = true)
        public List<AtencionResponse> listar() {

                return repository.findAll()
                        .stream()
                        .map(mapper::toDto)
                        .toList();
        }

        public AtencionResponse buscarPorId(Integer id) {

                Atencion atencion = repository.findById(id)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "La atención no existe"));

                return mapper.toDto(atencion);
        }

        public List<AtencionResponse> listarPorEstado(
                EstadoAtencion estado) {

                return repository.findByEstado(estado)
                        .stream()
                        .map(mapper::toDto)
                        .toList();
        }

        public List<AtencionResponse> listarPorPedido(
                Integer idPedido) {

                if (!pedidoRepository.existsById(idPedido)) {
                throw new NoSuchElementException(
                        "El pedido no existe");
                }

                return repository.findByPedidoIdPedido(idPedido)
                        .stream()
                        .map(mapper::toDto)
                        .toList();
        }

        @Transactional
        public AtencionResponse crear(
                AtencionRequest request,
                String correo) {

                Programador programador = programadorRepository
                        .findByTrabajadorUsuarioCorreo(correo)
                        .orElseThrow(() ->
                                new NoSuchElementException("Programador no encontrado"));

                Pedido pedido = pedidoRepository
                        .findById(request.idPedido())
                        .orElseThrow(() ->
                                new NoSuchElementException("El pedido no existe"));

                if (pedido.getEstado() != EstadoPedido.RECIBIDO
                        && pedido.getEstado() != EstadoPedido.EN_PREPARACION) {
                        throw new IllegalArgumentException("Solo se pueden programar pedidos pendientes");
                }
                if (repository.findByPedidoIdPedido(pedido.getIdPedido()).stream()
                        .anyMatch(this::estaActiva)) {
                        throw new IllegalArgumentException("El pedido ya tiene una atención activa");
                }

                Conductor conductor = conductorRepository
                        .findById(request.idConductor())
                        .orElseThrow(() ->
                                new NoSuchElementException( "El conductor no existe"));

                Cisterna cisterna = cisternaRepository
                        .findById(request.idCisterna())
                        .orElseThrow(() ->
                                new NoSuchElementException("La cisterna no existe"));

                if (conductor.getTrabajador().getEstado()!= Estado.ACTIVO) {
                        throw new IllegalArgumentException("El conductor no está activo");
                }

                if (cisterna.getEstado()!= Estado.ACTIVO) {
                        throw new IllegalArgumentException("La cisterna no está activa");
                }

                if (!request.fechaFinProgramada().isAfter(request.fechaInicioProgramada())) {
                        throw new IllegalArgumentException(
                        "La fecha de fin programada no puede ser anterior "
                        + "a la fecha de inicio programada");
                }

                if (repository.findByCisterna_IdCisterna(request.idCisterna()).stream()
                        .anyMatch(a -> seCruza(a, request))) {
                        throw new IllegalArgumentException("La cisterna no está disponible en ese horario");
                }
                if (repository.findByConductor_IdTrabajador(request.idConductor()).stream()
                        .anyMatch(a -> seCruza(a, request))) {
                        throw new IllegalArgumentException("El conductor no está disponible en ese horario");
                }

                Atencion atencion = mapper.toEntity(request);

                atencion.setPedido(pedido);
                atencion.setConductor(conductor);
                atencion.setCisterna(cisterna);
                atencion.setProgramador(programador);
                atencion.setEstado(EstadoAtencion.PROGRAMADA);

                Atencion guardada = repository.save(atencion);
                actualizarPedido(pedido, EstadoPedido.EN_PREPARACION,
                        "Atención programada: conductor, cisterna y horario asignados");

                return mapper.toDto(guardada);
        }

        private boolean estaActiva(Atencion atencion) {
                return atencion.getEstado() == EstadoAtencion.PROGRAMADA
                        || atencion.getEstado() == EstadoAtencion.EN_CURSO;
        }

        @Transactional
        public AtencionResponse editar(Integer id, EditarAtencionRequest request) {
                Atencion atencion = repository.findById(id)
                        .orElseThrow(() -> new NoSuchElementException("La atención no existe"));
                if (atencion.getEstado() != EstadoAtencion.PROGRAMADA) {
                        throw new IllegalArgumentException("Solo se puede editar una atención PROGRAMADA");
                }
                if (!request.fechaFinProgramada().isAfter(request.fechaInicioProgramada())) {
                        throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
                }
                Conductor conductor = conductorRepository.findById(request.idConductor())
                        .orElseThrow(() -> new NoSuchElementException("El conductor no existe"));
                Cisterna cisterna = cisternaRepository.findById(request.idCisterna())
                        .orElseThrow(() -> new NoSuchElementException("La cisterna no existe"));
                if (conductor.getTrabajador().getEstado() != Estado.ACTIVO) {
                        throw new IllegalArgumentException("El conductor no está activo");
                }
                if (cisterna.getEstado() != Estado.ACTIVO) {
                        throw new IllegalArgumentException("La cisterna no está activa");
                }
                AtencionRequest intervalo = new AtencionRequest(atencion.getPedido().getIdPedido(),
                        request.idConductor(), request.idCisterna(), request.fechaInicioProgramada(),
                        request.fechaFinProgramada(), request.observaciones());
                if (repository.findByCisterna_IdCisterna(request.idCisterna()).stream()
                        .anyMatch(a -> !id.equals(a.getIdAtencion()) && seCruza(a, intervalo))) {
                        throw new IllegalArgumentException("La cisterna no está disponible en ese horario");
                }
                if (repository.findByConductor_IdTrabajador(request.idConductor()).stream()
                        .anyMatch(a -> !id.equals(a.getIdAtencion()) && seCruza(a, intervalo))) {
                        throw new IllegalArgumentException("El conductor no está disponible en ese horario");
                }
                atencion.setConductor(conductor);
                atencion.setCisterna(cisterna);
                atencion.setFechaInicioProgramada(request.fechaInicioProgramada());
                atencion.setFechaFinProgramada(request.fechaFinProgramada());
                atencion.setObservaciones(request.observaciones());
                return mapper.toDto(repository.save(atencion));
        }

        private boolean seCruza(Atencion atencion, AtencionRequest request) {
                return estaActiva(atencion) && (atencion.getFechaFinProgramada() == null
                        || (atencion.getFechaInicioProgramada().isBefore(request.fechaFinProgramada())
                        && atencion.getFechaFinProgramada().isAfter(request.fechaInicioProgramada())));
        }

        @Transactional
        public AtencionResponse iniciar(Integer id, String correo) {

                // Serializa las aceptaciones del mismo conductor, incluso desde dos pestañas.
                Conductor conductor = conductorRepository.bloquearPorCorreo(correo)
                        .orElseThrow(() -> new NoSuchElementException("Conductor no encontrado"));
                Atencion atencion = asignada(id, correo);

                if (atencion.getEstado() != EstadoAtencion.PROGRAMADA) {
                        throw new IllegalArgumentException("Solo se puede iniciar una atención PROGRAMADA");
                }

                exigirEstadoPedido(atencion, EstadoPedido.EN_PREPARACION);
                if (repository.existsByConductor_IdTrabajadorAndEstado(
                        conductor.getIdTrabajador(), EstadoAtencion.EN_CURSO)) {
                        throw new IllegalArgumentException("Ya tienes una atención EN_CURSO. Finalízala antes de aceptar otra");
                }
                atencion.setEstado(EstadoAtencion.EN_CURSO);
                atencion.setFechaInicio(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS));

                Atencion actualizada = repository.save(atencion);
                actualizarPedido(atencion.getPedido(), EstadoPedido.DESPACHADO,
                        "El conductor inició el recorrido");

                return mapper.toDto(actualizada);
        }

        @Transactional
        public AtencionResponse finalizar(Integer id, String correo) {

        Atencion atencion = asignada(id, correo);

        if (atencion.getEstado()
                != EstadoAtencion.EN_CURSO) {

            throw new IllegalArgumentException(
                    "Solo se puede finalizar una atención EN_CURSO");
        }

        exigirEstadoPedido(atencion, EstadoPedido.DESPACHADO);
        atencion.setEstado(EstadoAtencion.FINALIZADA);
        atencion.setFechaFin(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS));

        Atencion actualizada = repository.save(atencion);
        actualizarPedido(atencion.getPedido(), EstadoPedido.ENTREGADO,
                "El conductor confirmó la entrega");

        return mapper.toDto(actualizada);
    }

        private void exigirEstadoPedido(Atencion atencion, EstadoPedido esperado) {
                if (atencion.getPedido().getEstado() != esperado) {
                        throw new IllegalArgumentException("El pedido debe estar " + esperado + " para realizar esta acción");
                }
        }

        private void actualizarPedido(Pedido pedido, EstadoPedido estado, String observacion) {
                if (pedido.getEstado() == estado) return;
                pedido.setEstado(estado);
                pedidoRepository.save(pedido);
                seguimientos.save(SeguimientoPedido.builder().pedido(pedido).estado(estado)
                        .fechaHora(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS))
                        .observacion(observacion).build());
        }

        @Transactional
        public AtencionResponse cancelar(Integer id,CancelarAtencionRequest request) {

                Atencion atencion = repository.findById(id).orElseThrow(() ->
                                new NoSuchElementException("La atención no existe"));

                if (atencion.getEstado()
                        != EstadoAtencion.PROGRAMADA) {

                throw new IllegalArgumentException(
                        "Solo se puede cancelar una atención PROGRAMADA");
                }

                exigirEstadoPedido(atencion, EstadoPedido.EN_PREPARACION);
                if (request.observaciones() == null || request.observaciones().isBlank()
                        || request.observaciones().length() > 250) {
                        throw new IllegalArgumentException("Ingresa un motivo de cancelación de hasta 250 caracteres");
                }
                atencion.setEstado(EstadoAtencion.CANCELADA);
                atencion.setObservaciones(request.observaciones().strip());

                Atencion actualizada = repository.save(atencion);
                actualizarPedido(atencion.getPedido(), EstadoPedido.RECIBIDO,
                        request.observaciones().strip());

                return mapper.toDto(actualizada);
        }
}
