package com.without.without_backend.controllers.atencion;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.NoSuchElementException;

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
import com.without.without_backend.handler.GlobalExceptionHandler;
import com.without.without_backend.services.JwtService;
import com.without.without_backend.services.atencion.AlertaService;

/** Privacidad de alertas: el conductor solo accede a las de sus propias atenciones. */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = AlertaSecurityTest.Config.class)
class AlertaSecurityTest {
    private static final String CONDUCTOR = "conductor1@example.test";

    @Configuration
    @EnableWebMvc
    @Import({SecurityConfig.class, JwtAuthFilter.class, AlertaRestController.class, GlobalExceptionHandler.class})
    static class Config {
        @Bean AlertaService service() { return mock(AlertaService.class); }
        @Bean JwtService jwt() { return mock(JwtService.class); }
        @Bean UserDetailsService users() { return mock(UserDetailsService.class); }
    }

    @Autowired WebApplicationContext context;
    @Autowired AlertaService service;
    MockMvc mvc;

    @BeforeEach void preparar() {
        reset(service);
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test void conductorConsultaSoloSusAlertasConElCorreoDeLaSesion() throws Exception {
        when(service.listarMisAlertas(CONDUCTOR)).thenReturn(List.of());
        mvc.perform(get("/api/v1/alertas/mis-alertas").with(user(CONDUCTOR).roles("CONDUCTOR"))).andExpect(status().isOk());
        verify(service).listarMisAlertas(CONDUCTOR);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/v1/alertas", "/api/v1/alertas/1", "/api/v1/alertas/estado?estado=PENDIENTE", "/api/v1/alertas/tipo?tipo=RETRASO"})
    void conductorYaNoVeAlertasDeOtros(String url) throws Exception {
        mvc.perform(get(url).with(user(CONDUCTOR).roles("CONDUCTOR"))).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "ANALISTA", "PROGRAMADOR"})
    void gestionSigueViendoTodas(String rol) throws Exception {
        when(service.listar()).thenReturn(List.of());
        mvc.perform(get("/api/v1/alertas").with(user("x@example.test").roles(rol))).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "PROGRAMADOR", "CLIENTE"})
    void misAlertasEsSoloDelConductor(String rol) throws Exception {
        mvc.perform(get("/api/v1/alertas/mis-alertas").with(user("x@example.test").roles(rol))).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test void conductorEnAlertasDeAtencionUsaLaVersionQueVerificaPropiedad() throws Exception {
        when(service.listarPorAtencionDelConductor(7, CONDUCTOR)).thenThrow(new NoSuchElementException("La atención no existe o no está asignada a tu cuenta"));
        mvc.perform(get("/api/v1/alertas/atencion/7").with(user(CONDUCTOR).roles("CONDUCTOR"))).andExpect(status().isNotFound());
        verify(service).listarPorAtencionDelConductor(7, CONDUCTOR);
        verify(service, never()).listarPorAtencion(anyInt());
    }

    @Test void programadorEnAlertasDeAtencionVeCualquiera() throws Exception {
        when(service.listarPorAtencion(7)).thenReturn(List.of());
        mvc.perform(get("/api/v1/alertas/atencion/7").with(user("p@example.test").roles("PROGRAMADOR"))).andExpect(status().isOk());
        verify(service, never()).listarPorAtencionDelConductor(anyInt(), any());
    }

    @Test void soloElConductorReportaAlertas() throws Exception {
        String cuerpo = "{\"tipo\":\"RETRASO\",\"mensaje\":\"Trafico\",\"idAtencion\":7}";
        mvc.perform(post("/api/v1/alertas").contentType("application/json").content(cuerpo).with(user("p@example.test").roles("PROGRAMADOR")))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/alertas").contentType("application/json").content(cuerpo).with(user(CONDUCTOR).roles("CONDUCTOR")))
            .andExpect(status().isCreated());
        verify(service).crear(any(), eq(CONDUCTOR));
    }
}
