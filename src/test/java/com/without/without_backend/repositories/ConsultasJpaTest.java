package com.without.without_backend.repositories;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.without.without_backend.repositories.atencion.AlertaRepository;
import com.without.without_backend.repositories.cisterna.FallaRepository;
import com.without.without_backend.repositories.cliente.ClienteRepository;
import com.without.without_backend.repositories.pedido.DetallePedidoRepository;
import com.without.without_backend.repositories.pedido.PedidoRepository;
import com.without.without_backend.support.TestDatabaseInitializer;
import com.without.without_backend.utils.Estado;
import com.without.without_backend.utils.pedido.EstadoPedido;

/**
 * Ejecuta contra MySQL real (withoutdb_test) las consultas personalizadas de los repositorios.
 * Los mocks no detectan un @EntityGraph o un método derivado que apunta a un atributo inexistente:
 * eso solo falla al ejecutarse y en la API aparece como error 500 (caso real: GET /api/v1/fallas).
 */
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestDatabaseInitializer.class)
@Transactional
class ConsultasJpaTest {
    @Autowired private FallaRepository fallas;
    @Autowired private PedidoRepository pedidos;
    @Autowired private ClienteRepository clientes;
    @Autowired private DetallePedidoRepository detalles;
    @Autowired private AlertaRepository alertas;

    @Test void alertasDelConductorPorCorreoSeConstruye() {
        assertTrue(alertas.findByAtencionConductorTrabajadorUsuarioCorreoOrderByFechaHoraDesc("nadie@example.test").isEmpty());
    }

    @Test void fallasCargaCisternaYTecnicoSinError() {
        assertDoesNotThrow(() -> fallas.findAll());
        assertDoesNotThrow(() -> fallas.findByCisterna_IdCisterna(1));
    }

    @Test void consultasPaginadasDePedidosSeConstruyen() {
        var pagina = PageRequest.of(0, 5);
        assertDoesNotThrow(() -> pedidos.findAll(pagina));
        assertDoesNotThrow(() -> pedidos.findByEstado(EstadoPedido.RECIBIDO, pagina));
        assertDoesNotThrow(() -> pedidos.findByClienteIdCliente(1, pagina));
        assertDoesNotThrow(() -> pedidos.findByClienteIdClienteAndEstado(1, EstadoPedido.RECIBIDO, pagina));
    }

    @Test void consultasDeReglasDeNegocioSeConstruyen() {
        assertFalse(clientes.existsByRuc("00000000000"));
        assertFalse(clientes.existsByRucAndIdClienteNot("00000000000", 1));
        assertEquals(0, detalles.countByPedidoIdPedidoAndEstado(-1, Estado.ACTIVO));
    }
}
