package com.without.without_backend.controllers.atencion;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import com.without.without_backend.services.JwtService;
import com.without.without_backend.services.atencion.AtencionService;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = AtencionClienteSecurityTest.Config.class)
class AtencionClienteSecurityTest {
    @Configuration @EnableWebMvc
    @Import({SecurityConfig.class, JwtAuthFilter.class, AtencionRestController.class})
    static class Config {
        @Bean AtencionService service() { return mock(AtencionService.class); }
        @Bean JwtService jwt() { return mock(JwtService.class); }
        @Bean UserDetailsService users() { return mock(UserDetailsService.class); }
    }
    @Autowired WebApplicationContext context;
    @Autowired AtencionService service;
    MockMvc mvc;
    @BeforeEach void setup() {
        reset(service);
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }
    @Test void sinSesionSeRechaza() throws Exception {
        mvc.perform(get("/api/v1/atenciones/mis-atenciones")).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }
    @Test void clienteSoloConsultaSuCorreoAunqueEnvieOtro() throws Exception {
        when(service.listarMisAtenciones("cliente@without.example")).thenReturn(List.of());
        mvc.perform(get("/api/v1/atenciones/mis-atenciones").param("correo", "ajeno@example.com")
            .with(user("cliente@without.example").roles("CLIENTE"))).andExpect(status().isOk());
        verify(service).listarMisAtenciones("cliente@without.example");
        verifyNoMoreInteractions(service);
    }
    @Test void clienteNoPuedeConsultarTodasLasAtenciones() throws Exception {
        mvc.perform(get("/api/v1/atenciones").with(user("cliente@without.example").roles("CLIENTE")))
            .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    @Test void rolInternoNoPuedeSuplantarCliente() throws Exception {
        mvc.perform(get("/api/v1/atenciones/mis-atenciones").with(user("admin@example.com").roles("ADMIN")))
            .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test void conductorConsultaSoloSusAsignadas() throws Exception {
        when(service.listarAsignadas("conductor@example.com")).thenReturn(List.of());
        mvc.perform(get("/api/v1/atenciones/asignadas").param("correo", "ajeno@example.com")
            .with(user("conductor@example.com").roles("CONDUCTOR"))).andExpect(status().isOk());
        verify(service).listarAsignadas("conductor@example.com");
        verifyNoMoreInteractions(service);
    }
    @Test void conductorNoConsultaListadoGeneral() throws Exception {
        mvc.perform(get("/api/v1/atenciones").with(user("conductor@example.com").roles("CONDUCTOR")))
            .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    @Test void clienteNoConsultaAsignadasDeConductor() throws Exception {
        mvc.perform(get("/api/v1/atenciones/asignadas").with(user("cliente@example.com").roles("CLIENTE")))
            .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test void programadorPuedeEditarAtencion() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/atenciones/8")
            .contentType("application/json")
            .content("{\"idConductor\":2,\"idCisterna\":3,\"fechaInicioProgramada\":\"2031-01-01T10:00:00\",\"fechaFinProgramada\":\"2031-01-01T11:00:00\",\"observaciones\":\"Corrección\"}")
            .with(user("programador@example.com").roles("PROGRAMADOR"))).andExpect(status().isOk());
        verify(service).editar(eq(8), any(com.without.without_backend.dto.atencion.atencion.EditarAtencionRequest.class));
    }

    @Test void otrosRolesNoPuedenEditarAtencion() throws Exception {
        for (String rol : List.of("CLIENTE", "CONDUCTOR", "ADMIN", "ANALISTA", "TECNICO")) {
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/atenciones/8")
                .contentType("application/json").content("{}")
                .with(user("usuario@example.com").roles(rol))).andExpect(status().isForbidden());
        }
        verifyNoInteractions(service);
    }
}
