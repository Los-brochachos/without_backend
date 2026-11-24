package com.without.without_backend.services.pedido;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.without.without_backend.dto.pedido.detallepedido.AgregarCantidadDetalleRequest;
import com.without.without_backend.dto.pedido.detallepedido.DetallePedidoRequest;
import com.without.without_backend.dto.pedido.detallepedido.DetallePedidoResponse;
import com.without.without_backend.entities.cliente.Cliente;
import com.without.without_backend.entities.pedido.DetallePedido;
import com.without.without_backend.entities.pedido.Pedido;
import com.without.without_backend.entities.pedido.Producto;
import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.mappers.pedido.DetallePedidoMapper;
import com.without.without_backend.repositories.cliente.ClienteRepository;
import com.without.without_backend.repositories.pedido.DetallePedidoRepository;
import com.without.without_backend.repositories.pedido.PedidoRepository;
import com.without.without_backend.repositories.pedido.ProductoRepository;
import com.without.without_backend.repositories.usuario.UsuarioRepository;
import com.without.without_backend.utils.Estado;
import com.without.without_backend.utils.pedido.EstadoPedido;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DetallePedidoService {

        private final DetallePedidoRepository detallePedidoRepository;
        private final PedidoRepository pedidoRepository;
        private final ProductoRepository productoRepository;
        private final DetallePedidoMapper detallePedidoMapper;
        private final UsuarioRepository usuarioRepository;
        private final ClienteRepository clienteRepository;

        @Transactional(readOnly = true)
        public List<DetallePedidoResponse> listarActivosPorPedidoCliente(
                Integer idPedido, String correo) {

                Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() ->
                        new NoSuchElementException("Usuario no encontrado"));

                Cliente cliente = clienteRepository.findByUsuario(usuario)
                        .orElseThrow(() ->
                                new NoSuchElementException("Cliente no encontrado"));

                pedidoRepository.findByIdPedidoAndClienteIdCliente(
                        idPedido,
                        cliente.getIdCliente()
                ).orElseThrow(() ->
                        new NoSuchElementException("Pedido no encontrado"));

                return detallePedidoRepository
                        .findByPedidoIdPedidoAndEstado(
                                idPedido,
                                Estado.ACTIVO)
                        .stream()
                        .map(detallePedidoMapper::toResponse)
                        .toList();
        }

        @Transactional(readOnly = true)
        public List<DetallePedidoResponse> listarActivosPorPedido(
                Integer idPedido) {

          
                if (!pedidoRepository.existsById(idPedido)) {
                        throw new NoSuchElementException("Pedido no encontrado");
                }


                return detallePedidoRepository
                        .findByPedidoIdPedidoAndEstado(
                                idPedido,
                                Estado.ACTIVO)
                        .stream()
                        .map(detallePedidoMapper::toResponse)
                        .toList();
        }


        @Transactional(readOnly = true)
        public List<DetallePedidoResponse> listarTodosPorPedido(
                Integer idPedido) {

                if (!pedidoRepository.existsById(idPedido)) {
                throw new NoSuchElementException("Pedido no encontrado");
                }

                return detallePedidoRepository
                        .findByPedidoIdPedido(idPedido)
                        .stream()
                        .map(detallePedidoMapper::toResponse)
                        .toList();
        }

        @Transactional
        public DetallePedidoResponse agregarDetalle(
                Integer idPedido,
                DetallePedidoRequest request) {

                Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() ->
                        new NoSuchElementException("Pedido no encontrado"));

                return agregarDetalleValidado(pedido, request);
        }


        @Transactional
        public DetallePedidoResponse agregarDetalleCliente(
                Integer idPedido,
                DetallePedidoRequest request,
                String correo) {

                Usuario usuario = usuarioRepository.findByCorreo(correo)
                        .orElseThrow(() ->
                                new NoSuchElementException("Usuario no encontrado"));

                Cliente cliente = clienteRepository.findByUsuario(usuario)
                        .orElseThrow(() ->
                                new NoSuchElementException("Cliente no encontrado"));

                Pedido pedido = pedidoRepository
                        .findByIdPedidoAndClienteIdCliente(
                                idPedido,
                                cliente.getIdCliente()
                        )
                .orElseThrow(() ->
                        new NoSuchElementException("Pedido no encontrado"));


                return agregarDetalleValidado(pedido, request);
        }

        @Transactional
        public DetallePedidoResponse agregarCantidadCliente(
                Integer idPedido,
                Integer idDetalle,
                AgregarCantidadDetalleRequest request,
                String correo) {

                Usuario usuario = usuarioRepository.findByCorreo(correo)
                        .orElseThrow(() ->
                                new NoSuchElementException("Usuario no encontrado"));

                Cliente cliente = clienteRepository.findByUsuario(usuario)
                        .orElseThrow(() ->
                                new NoSuchElementException("Cliente no encontrado"));

                Pedido pedido = pedidoRepository
                        .findByIdPedidoAndClienteIdCliente(
                                idPedido,
                                cliente.getIdCliente())
                        .orElseThrow(() ->
                                new NoSuchElementException("Pedido no encontrado"));

                return agregarCantidadValidado(pedido, idPedido,  idDetalle,  request);
        }

        @Transactional
        public DetallePedidoResponse agregarCantidad(Integer idPedido,Integer idDetalle,AgregarCantidadDetalleRequest request) {

                Pedido pedido = pedidoRepository.findById(idPedido)
                        .orElseThrow(() ->
                                new NoSuchElementException("Pedido no encontrado"));

                return agregarCantidadValidado(pedido,idPedido,idDetalle,request);
        }
        @Transactional
        public DetallePedidoResponse cancelarCliente(Integer idPedido,Integer idDetalle,String correo) {

                Usuario usuario = usuarioRepository.findByCorreo(correo)
                        .orElseThrow(() ->
                                new NoSuchElementException("Usuario no encontrado"));

                Cliente cliente = clienteRepository.findByUsuario(usuario)
                        .orElseThrow(() ->
                                new NoSuchElementException("Cliente no encontrado"));

                Pedido pedido = pedidoRepository
                        .findByIdPedidoAndClienteIdCliente(
                                idPedido,
                                cliente.getIdCliente())
                        .orElseThrow(() ->
                                new NoSuchElementException("Pedido no encontrado"));

                return cancelarValidado(pedido,idPedido,idDetalle);
        }


        @Transactional
        public DetallePedidoResponse cancelar(Integer idPedido,Integer idDetalle) {

                Pedido pedido = pedidoRepository.findById(idPedido).orElseThrow(() ->
                                        new NoSuchElementException("Pedido no encontrado"));

                return cancelarValidado(pedido,idPedido,idDetalle);
        }

        private void validarPedidoEditable(Pedido pedido) {

                if (pedido.getEstado() != EstadoPedido.RECIBIDO) {
                        throw new IllegalArgumentException("Solo se pueden modificar los detalles de un pedido en estado RECIBIDO");
                }
        }

        private DetallePedido obtenerDetalle(Integer idPedido, Integer idDetalle) {

                DetallePedido detalle = detallePedidoRepository
                        .findById(idDetalle)
                        .orElseThrow(() ->
                                new NoSuchElementException("Detalle de pedido no encontrado"));

                if (!detalle.getPedido().getIdPedido().equals(idPedido)) {
                        throw new IllegalArgumentException("El detalle no pertenece al pedido indicado");
                }

                return detalle;
        }

        private void validarDetalleActivo(
                DetallePedido detalle) {

                if (detalle.getEstado() != Estado.ACTIVO) {
                        throw new IllegalArgumentException("El detalle está inactivo");
                }
        }

        private DetallePedidoResponse agregarDetalleValidado(
                Pedido pedido,
                DetallePedidoRequest request) {

                validarPedidoEditable(pedido);

                Producto producto = productoRepository
                        .findById(request.idProducto())
                        .orElseThrow(() ->
                                new NoSuchElementException("Producto no encontrado"));

                if (producto.getEstado() != Estado.ACTIVO) {
                        throw new IllegalArgumentException(
                                "El producto no está activo");
                }

                boolean productoYaExiste = detallePedidoRepository
                                                .existsByPedidoIdPedidoAndProductoIdProductoAndEstado(
                                                        pedido.getIdPedido(),
                                                        request.idProducto(),
                                                        Estado.ACTIVO);

                if (productoYaExiste) {
                        throw new IllegalArgumentException(
                                "El producto ya existe en el pedido. Puede agregar más cantidad");
                }

                DetallePedido detalle = detallePedidoMapper.toEntity(request,pedido,producto);
                detalle.setEstado(Estado.ACTIVO);
                DetallePedido guardado = detallePedidoRepository.save(detalle);

                return detallePedidoMapper.toResponse(guardado);
        }

        private DetallePedidoResponse agregarCantidadValidado(Pedido pedido,Integer idPedido,Integer idDetalle,AgregarCantidadDetalleRequest request) {
                validarPedidoEditable(pedido);

                DetallePedido detalle = obtenerDetalle(idPedido,idDetalle);

                validarDetalleActivo(detalle);

                detalle.setCantidad(detalle.getCantidad().add(request.cantidad()));
                DetallePedido actualizado = detallePedidoRepository.save(detalle);

                return detallePedidoMapper.toResponse(actualizado);
        }

        private DetallePedidoResponse cancelarValidado(Pedido pedido,Integer idPedido,Integer idDetalle) {

                validarPedidoEditable(pedido);
                DetallePedido detalle = obtenerDetalle(idPedido,idDetalle);

                validarDetalleActivo(detalle);

                // Un pedido no puede quedar vacío: para eso existe la cancelación del pedido.
                if (detallePedidoRepository.countByPedidoIdPedidoAndEstado(idPedido, Estado.ACTIVO) <= 1) {
                        throw new IllegalArgumentException(
                                "El pedido debe conservar al menos un producto. Si ya no lo necesitas, cancela el pedido");
                }

                detalle.setEstado(Estado.INACTIVO);
                
                DetallePedido actualizado = detallePedidoRepository.save(detalle);

                return detallePedidoMapper.toResponse(actualizado);
        }


        
}
