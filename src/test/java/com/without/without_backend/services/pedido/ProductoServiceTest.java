package com.without.without_backend.services.pedido;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.without.without_backend.dto.pedido.producto.ProductoCreateRequest;
import com.without.without_backend.dto.pedido.producto.ProductoUpdateRequest;
import com.without.without_backend.entities.pedido.Producto;
import com.without.without_backend.mappers.pedido.ProductoMapper;
import com.without.without_backend.repositories.pedido.ProductoRepository;
import com.without.without_backend.utils.Estado;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductoService: reglas del catalogo")
class ProductoServiceTest {

    @Mock
    private ProductoRepository repository;
    private ProductoService service;

    @BeforeEach
    void prepararServicio() {
        // Repositorio simulado y mapper real: no se inicia Spring ni MySQL.
        service = new ProductoService(repository, new ProductoMapper());
    }

    @Test
    @DisplayName("P01 - Normal: crea un producto activo y devuelve los datos guardados")
    void crearProductoNuevoLoGuardaActivo() {
        var request = new ProductoCreateRequest("Oxigeno", "O2", "m3", new BigDecimal("8.50"));
        when(repository.findByNombre("Oxigeno")).thenReturn(Optional.empty());
        when(repository.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto producto = invocation.getArgument(0);
            producto.setIdProducto(10);
            return producto;
        });

        var response = service.crearProducto(request);

        var captor = ArgumentCaptor.forClass(Producto.class);
        verify(repository).save(captor.capture());
        assertAll(
            () -> assertEquals(Estado.ACTIVO, captor.getValue().getEstado()),
            () -> assertEquals(10, response.idProducto()),
            () -> assertEquals("Oxigeno", response.nombre()),
            () -> assertEquals("O2", response.tipoGas()),
            () -> assertEquals("m3", response.unidadMedida()),
            () -> assertEquals(new BigDecimal("8.50"), response.precioUnitario()),
            () -> assertEquals(Estado.ACTIVO, response.estado())
        );
    }

    @Test
    @DisplayName("P02 - Alternativo: rechaza un nombre duplicado sin guardar")
    void crearProductoConNombreDuplicadoFalla() {
        var request = new ProductoCreateRequest("Oxigeno", "O2", "m3", new BigDecimal("8.50"));
        when(repository.findByNombre("Oxigeno")).thenReturn(Optional.of(producto()));

        var error = assertThrows(IllegalArgumentException.class, () -> service.crearProducto(request));

        assertEquals("Ya existe un producto con ese nombre", error.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("P03 - Alternativo: buscar un ID inexistente informa que no existe")
    void buscarProductoInexistenteFalla() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        var error = assertThrows(NoSuchElementException.class, () -> service.buscarPorId(999));

        assertEquals("Producto no encontrado", error.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("P04 - Normal: actualiza solo el precio y conserva los otros campos")
    void actualizarSoloPrecioConservaLosOtrosDatos() {
        Producto producto = producto();
        when(repository.findById(10)).thenReturn(Optional.of(producto));
        when(repository.save(producto)).thenReturn(producto);

        var response = service.actualizarProducto(10,
            new ProductoUpdateRequest(null, null, null, new BigDecimal("9.25")));

        assertAll(
            () -> assertEquals(new BigDecimal("9.25"), response.precioUnitario()),
            () -> assertEquals("Oxigeno", response.nombre()),
            () -> assertEquals("O2", response.tipoGas()),
            () -> assertEquals("m3", response.unidadMedida()),
            () -> assertEquals(Estado.ACTIVO, response.estado())
        );
        verify(repository).save(producto);
        verify(repository, never()).findByNombre(any());
    }

    @Test
    @DisplayName("P05 - Limite: actualizar sin campos conserva todos los valores")
    void actualizarSinCamposNoModificaElProducto() {
        Producto producto = producto();
        when(repository.findById(10)).thenReturn(Optional.of(producto));
        when(repository.save(producto)).thenReturn(producto);
        var esperado = new ProductoMapper().toResponse(producto);

        var response = service.actualizarProducto(10, new ProductoUpdateRequest(null, null, null, null));

        assertEquals(esperado, response);
        verify(repository, never()).findByNombre(any());
    }

    @Test
    @DisplayName("P06 - Limite: conservar el mismo nombre no se considera duplicado")
    void actualizarConElMismoNombreNoBuscaDuplicados() {
        Producto producto = producto();
        when(repository.findById(10)).thenReturn(Optional.of(producto));
        when(repository.save(producto)).thenReturn(producto);

        var response = service.actualizarProducto(10, new ProductoUpdateRequest("Oxigeno", null, null, null));

        assertEquals("Oxigeno", response.nombre());
        verify(repository, never()).findByNombre(any());
        verify(repository).save(producto);
    }

    @Test
    @DisplayName("P07 - Normal: eliminar desactiva el producto sin borrarlo fisicamente")
    void eliminarProductoRealizaBajaLogica() {
        Producto producto = producto();
        when(repository.findById(10)).thenReturn(Optional.of(producto));

        service.eliminarProducto(10);

        assertEquals(Estado.INACTIVO, producto.getEstado());
        verify(repository).save(producto);
        verify(repository, never()).delete(any());
        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("P08 - Alternativo: renombrar a un nombre ocupado no modifica ni guarda")
    void renombrarConNombreDuplicadoFallaSinModificar() {
        Producto producto = producto();
        when(repository.findById(10)).thenReturn(Optional.of(producto));
        when(repository.findByNombre("Argon")).thenReturn(Optional.of(Producto.builder().idProducto(11).build()));

        assertThrows(IllegalArgumentException.class, () -> service.actualizarProducto(10,
            new ProductoUpdateRequest("Argon", "Ar", "kg", new BigDecimal("20.00"))));

        assertAll(
            () -> assertEquals("Oxigeno", producto.getNombre()),
            () -> assertEquals("O2", producto.getTipoGas()),
            () -> assertEquals("m3", producto.getUnidadMedida()),
            () -> assertEquals(new BigDecimal("8.50"), producto.getPrecioUnitario())
        );
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("P09 - Normal: permite cambiar a un nombre disponible")
    void renombrarConNombreDisponibleGuardaElCambio() {
        Producto producto = producto();
        when(repository.findById(10)).thenReturn(Optional.of(producto));
        when(repository.findByNombre("Oxigeno medicinal")).thenReturn(Optional.empty());
        when(repository.save(producto)).thenReturn(producto);

        var response = service.actualizarProducto(10,
            new ProductoUpdateRequest("Oxigeno medicinal", null, null, null));

        assertEquals("Oxigeno medicinal", response.nombre());
        assertEquals(new BigDecimal("8.50"), response.precioUnitario());
        verify(repository).save(producto);
    }

    @Test
    @DisplayName("P10 - Alternativo: actualizar un ID inexistente no guarda")
    void actualizarProductoInexistenteFalla() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.actualizarProducto(999,
            new ProductoUpdateRequest("Argon", null, null, null)));

        verify(repository, never()).findByNombre(any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("P11 - Alternativo: eliminar un ID inexistente no guarda ni borra")
    void eliminarProductoInexistenteFalla() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.eliminarProducto(999));

        verify(repository, never()).save(any());
        verify(repository, never()).deleteById(any());
    }

    private Producto producto() {
        return Producto.builder().idProducto(10).nombre("Oxigeno").tipoGas("O2")
            .unidadMedida("m3").precioUnitario(new BigDecimal("8.50")).estado(Estado.ACTIVO).build();
    }
}
