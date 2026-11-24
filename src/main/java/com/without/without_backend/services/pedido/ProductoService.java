package com.without.without_backend.services.pedido;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.without.without_backend.dto.pedido.producto.ProductoCreateRequest;
import com.without.without_backend.dto.pedido.producto.ProductoResponse;
import com.without.without_backend.dto.pedido.producto.ProductoUpdateRequest;
import com.without.without_backend.entities.pedido.Producto;
import com.without.without_backend.mappers.pedido.ProductoMapper;
import com.without.without_backend.repositories.pedido.ProductoRepository;
import com.without.without_backend.utils.Estado;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;

    public List<ProductoResponse> listarTodos() {

        return productoRepository.findAll()
                .stream()
                .map(productoMapper::toResponse)
                .toList();
    }

    public List<ProductoResponse> listarActivos() {

        return productoRepository
                .findByEstado(Estado.ACTIVO)
                .stream()
                .map(productoMapper::toResponse)
                .toList();
    }

    public ProductoResponse buscarPorId(Integer id) {

        Producto producto = productoRepository
                .findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Producto no encontrado"));

        return productoMapper.toResponse(producto);
    }

    public ProductoResponse buscarActivoPorId(Integer id) {

        Producto producto = productoRepository
                .findByIdProductoAndEstado(id, Estado.ACTIVO)
                .orElseThrow(() ->
                        new NoSuchElementException("Producto no encontrado"));

        return productoMapper.toResponse(producto);
    }

    @Transactional
    public ProductoResponse crearProducto(
            ProductoCreateRequest request) {

        if (productoRepository
                .findByNombre(request.nombre())
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Ya existe un producto con ese nombre");
        }

        Producto producto = productoMapper.toEntity(request);

        producto.setEstado(Estado.ACTIVO);

        producto = productoRepository.save(producto);

        return productoMapper.toResponse(producto);
    }

    @Transactional
    public ProductoResponse actualizarProducto(
            Integer id,
            ProductoUpdateRequest request) {

        Producto producto = productoRepository
                .findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Producto no encontrado"));

        if (request.nombre() != null
                && !producto.getNombre().equals(request.nombre())
                && productoRepository
                        .findByNombre(request.nombre())
                        .isPresent()) {

            throw new IllegalArgumentException(
                    "Ya existe un producto con ese nombre");
        }

        if (request.nombre() != null) {
            producto.setNombre(request.nombre());
        }

        if (request.tipoGas() != null) {
            producto.setTipoGas(request.tipoGas());
        }

        if (request.unidadMedida() != null) {
            producto.setUnidadMedida(request.unidadMedida());
        }

        if (request.precioUnitario() != null) {
            producto.setPrecioUnitario(request.precioUnitario());
        }

        producto = productoRepository.save(producto);

        return productoMapper.toResponse(producto);
    }

    @Transactional
    public void eliminarProducto(Integer id) {

        Producto producto = productoRepository
                .findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Producto no encontrado"));

        producto.setEstado(Estado.INACTIVO);

        productoRepository.save(producto);
    }
}