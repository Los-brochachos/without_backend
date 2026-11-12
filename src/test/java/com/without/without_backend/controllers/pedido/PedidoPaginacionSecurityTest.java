package com.without.without_backend.controllers.pedido;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
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
import com.without.without_backend.dto.comun.PaginaResponse;
import com.without.without_backend.handler.GlobalExceptionHandler;
import com.without.without_backend.services.JwtService;
import com.without.without_backend.services.pedido.PedidoService;
import com.without.without_backend.utils.pedido.EstadoPedido;

/** Endpoints paginados de pedidos: permisos por rol y validación de parámetros. */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = PedidoPaginacionSecurityTest.Config.class)
class PedidoPaginacionSecurityTest {
    private static final String URL = "/api/v1/pedidos/paginado";
    private static final String URL_CLIENTE = "/api/v1/pedidos/mis-pedidos/paginado";
    private static final String CORREO = "cliente@example.test";

    @Configuration
    @EnableWebMvc
    @Import({SecurityConfig.class, JwtAuthFilter.class, PedidoRestController.class, GlobalExceptionHandler.class})
    static class Config {
        @Bean PedidoService service() { return mock(PedidoService.class); }
        @Bean JwtService jwt() { return mock(JwtService.class); }
        @Bean UserDetailsService users() { return mock(UserDetailsService.class); }
    }

    @Autowired WebApplicationContext context;
    @Autowired PedidoService service;
    MockMvc mvc;

    @BeforeEach void preparar() {
        reset(service);
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private static PaginaResponse<com.without.without_backend.dto.pedido.pedido.PedidoResponse> vacia() {
        return new PaginaResponse<>(List.of(), 0, 10, 0, 0, true, true);
    }

    @Test void sinSesionResponde401() throws Exception {
        mvc.perform(get(URL)).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "ANALISTA", "PROGRAMADOR"})
    void rolesDeGestionConsultanPaginado(String rol) throws Exception {
        when(service.listarPaginado(2, 5, EstadoPedido.RECIBIDO)).thenReturn(vacia());
        mvc.perform(get(URL).param("pagina", "2").param("tamanio", "5").param("estado", "RECIBIDO")
                .with(user("staff@example.test").roles(rol)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.contenido").isArray())
            .andExpect(jsonPath("$.totalPaginas").value(0));
        verify(service).listarPaginado(2, 5, EstadoPedido.RECIBIDO);
    }

    @Test void valoresPorDefectoSonPagina0Tamanio10() throws Exception {
        when(service.listarPaginado(0, 10, null)).thenReturn(vacia());
        mvc.perform(get(URL).with(user("a@example.test").roles("ADMIN"))).andExpect(status().isOk());
        verify(service).listarPaginado(0, 10, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CLIENTE", "CONDUCTOR", "TECNICO"})
    void otrosRolesNoVenTodosLosPedidos(String rol) throws Exception {
        mvc.perform(get(URL).with(user("x@example.test").roles(rol))).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test void clienteUsaCorreoDeLaSesion() throws Exception {
        when(service.listarMisPedidosPaginado(CORREO, 0, 10, null)).thenReturn(vacia());
        mvc.perform(get(URL_CLIENTE).param("correo", "otro@example.test").with(user(CORREO).roles("CLIENTE")))
            .andExpect(status().isOk());
        verify(service).listarMisPedidosPaginado(CORREO, 0, 10, null);
    }

    @Test void staffNoUsaPaginadoDeCliente() throws Exception {
        mvc.perform(get(URL_CLIENTE).with(user("a@example.test").roles("ADMIN")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("No tienes permiso para realizar esta accion"));
        verifyNoInteractions(service);
    }

    // ===== Cancelar mi pedido: solo CLIENTE y con el correo de la sesión =====
    private static final String URL_CANCELAR = "/api/v1/pedidos/mis-pedidos/10/cancelar";
    private static final String MOTIVO = "{\"observacion\":\"Ya no lo necesito\"}";

    @Test void clienteCancelaSuPedidoConElCorreoDeLaSesion() throws Exception {
        mvc.perform(patch(URL_CANCELAR).contentType("application/json").content(MOTIVO)
                .with(user(CORREO).roles("CLIENTE")))
            .andExpect(status().isOk());
        verify(service).cancelarMiPedido(eq(10), any(), eq(CORREO));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "ANALISTA", "PROGRAMADOR", "CONDUCTOR", "TECNICO"})
    void otrosRolesNoUsanCancelarMiPedido(String rol) throws Exception {
        mvc.perform(patch(URL_CANCELAR).contentType("application/json").content(MOTIVO)
                .with(user("x@example.test").roles(rol)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("No tienes permiso para realizar esta accion"));
        verifyNoInteractions(service);
    }

    @Test void cancelarMiPedidoSinSesionResponde401() throws Exception {
        mvc.perform(patch(URL_CANCELAR).contentType("application/json").content(MOTIVO))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test void parametrosInvalidosResponden400() throws Exception {
        when(service.listarPaginado(anyInt(), eq(500), any()))
            .thenThrow(new IllegalArgumentException("El tamaño de página debe estar entre 1 y 50"));
        mvc.perform(get(URL).param("tamanio", "500").with(user("a@example.test").roles("ADMIN")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("El tamaño de página debe estar entre 1 y 50"));
    }

    @Test void estadoInexistenteResponde400() throws Exception {
        mvc.perform(get(URL).param("estado", "VOLANDO").with(user("a@example.test").roles("ADMIN")))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
