package com.without.without_backend.controllers.pedido;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.without.without_backend.components.JwtAuthFilter;
import com.without.without_backend.config.SecurityConfig;
import com.without.without_backend.dto.pedido.detallepedido.AgregarCantidadDetalleRequest;
import com.without.without_backend.dto.pedido.detallepedido.DetallePedidoRequest;
import com.without.without_backend.handler.GlobalExceptionHandler;
import com.without.without_backend.services.JwtService;
import com.without.without_backend.services.pedido.DetallePedidoService;

/** Solo MVC y seguridad con servicio simulado: sin Boot, JPA, datasource ni seeder. */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = DetallePedidoSecurityTest.Config.class)
class DetallePedidoSecurityTest {
    private static final String URL = "/api/v1/pedidos/10/detalles";
    private static final String CORREO = "cliente@example.test";

    @Configuration
    @EnableWebMvc
    @Import({SecurityConfig.class, JwtAuthFilter.class, DetallePedidoRestController.class,
        GlobalExceptionHandler.class})
    static class Config {
        @Bean DetallePedidoService service() { return mock(DetallePedidoService.class); }
        @Bean JwtService jwt() { return mock(JwtService.class); }
        @Bean UserDetailsService users() { return mock(UserDetailsService.class); }
    }

    @Autowired WebApplicationContext context;
    @Autowired DetallePedidoService service;
    MockMvc mvc;

    @BeforeEach void preparar() {
        reset(service);
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test void sinSesionNoConsultaDetalles() throws Exception {
        mvc.perform(get(URL)).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test void correoDeConsultaNoSuplantaLaSesion() throws Exception {
        when(service.listarActivosPorPedidoCliente(10, CORREO)).thenReturn(List.of());
        mvc.perform(get(URL).param("correo", "otra-persona@example.test")
            .with(user(CORREO).roles("CLIENTE"))).andExpect(status().isOk());
        verify(service).listarActivosPorPedidoCliente(10, CORREO);
        verifyNoMoreInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "ANALISTA", "PROGRAMADOR", "CONDUCTOR", "TECNICO"})
    void otrosRolesNoAgreganDetallesComoCliente(String rol) throws Exception {
        mvc.perform(post(URL).contentType("application/json")
            .content("{\"cantidad\":1,\"idProducto\":4}")
            .with(user("personal@example.test").roles(rol)))
            .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test void clienteNoAccedeAlListadoAdministrativo() throws Exception {
        mvc.perform(get(URL + "/todos").with(user(CORREO).roles("CLIENTE")))
            .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "ANALISTA", "PROGRAMADOR"})
    void rolesAutorizadosConsultanDetallesAdministrativos(String rol) throws Exception {
        when(service.listarTodosPorPedido(10)).thenReturn(List.of());
        mvc.perform(get(URL + "/todos").with(user("personal@example.test").roles(rol)))
            .andExpect(status().isOk());
        verify(service).listarTodosPorPedido(10);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"-1", "0", "0.009"})
    void altaRechazaCantidadInvalidaAntesDeLlamarServicio(String cantidad) throws Exception {
        mvc.perform(post(URL).contentType("application/json")
            .content("{\"cantidad\":" + cantidad + ",\"idProducto\":4}")
            .with(user(CORREO).roles("CLIENTE")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.cantidad").exists());
        verifyNoInteractions(service);
    }

    @Test void productoObligatorioSeValidaEnHttp() throws Exception {
        mvc.perform(post(URL).contentType("application/json").content("{\"cantidad\":1}")
            .with(user(CORREO).roles("CLIENTE")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.idProducto").exists());
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"-1", "0", "0.009"})
    void incrementoRechazaCantidadInvalidaAntesDeLlamarServicio(String cantidad) throws Exception {
        mvc.perform(patch(URL + "/20/cantidad").contentType("application/json")
            .content("{\"cantidad\":" + cantidad + "}")
            .with(user(CORREO).roles("CLIENTE")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.cantidad").exists());
        verifyNoInteractions(service);
    }

    @Test void cantidadMinimaValidaSeEnviaConIdentidadDeSesion() throws Exception {
        mvc.perform(post(URL).contentType("application/json")
            .content("{\"cantidad\":0.01,\"idProducto\":4}")
            .with(user(CORREO).roles("CLIENTE"))).andExpect(status().isOk());
        verify(service).agregarDetalleCliente(10,
            new DetallePedidoRequest(new BigDecimal("0.01"), 4), CORREO);
    }

    @Test void aumentoValidoSeEnviaConPedidoDetalleYUsuario() throws Exception {
        mvc.perform(patch(URL + "/20/cantidad").contentType("application/json")
            .content("{\"cantidad\":0.01}")
            .with(user(CORREO).roles("CLIENTE"))).andExpect(status().isOk());
        verify(service).agregarCantidadCliente(10, 20,
            new AgregarCantidadDetalleRequest(new BigDecimal("0.01")), CORREO);
    }

    @Test void cancelarNoPermiteElegirElCorreoPorParametro() throws Exception {
        mvc.perform(patch(URL + "/20/cancelar").param("correo", "otra-persona@example.test")
            .with(user(CORREO).roles("CLIENTE"))).andExpect(status().isOk());
        verify(service).cancelarCliente(10, 20, CORREO);
    }

    @Test void pedidoAjenoONoEncontradoDevuelve404() throws Exception {
        when(service.listarActivosPorPedidoCliente(10, CORREO))
            .thenThrow(new NoSuchElementException("Pedido no encontrado"));
        mvc.perform(get(URL).with(user(CORREO).roles("CLIENTE")))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test void reglaDeNegocioInvalidaDevuelve400() throws Exception {
        when(service.cancelarCliente(10, 20, CORREO))
            .thenThrow(new IllegalArgumentException("El detalle esta inactivo"));
        mvc.perform(patch(URL + "/20/cancelar").with(user(CORREO).roles("CLIENTE")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }
}
