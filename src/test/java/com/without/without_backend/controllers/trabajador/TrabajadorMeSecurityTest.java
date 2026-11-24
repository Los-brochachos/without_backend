package com.without.without_backend.controllers.trabajador;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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
import com.without.without_backend.services.trabajador.TrabajadorService;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = TrabajadorMeSecurityTest.Config.class)
class TrabajadorMeSecurityTest {
    @Configuration @EnableWebMvc
    @Import({SecurityConfig.class, JwtAuthFilter.class, TrabajadorRestController.class})
    static class Config {
        @Bean TrabajadorService service() { return mock(TrabajadorService.class); }
        @Bean JwtService jwt() { return mock(JwtService.class); }
        @Bean UserDetailsService users() { return mock(UserDetailsService.class); }
    }
    @Autowired WebApplicationContext context;
    @Autowired TrabajadorService service;
    MockMvc mvc;
    @BeforeEach void setup() {
        reset(service); mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }
    @Test void personalConsultaSoloSuIdentidadAunqueEnvieOtroCorreo() throws Exception {
        for (String rol : new String[]{"ADMIN", "ANALISTA", "PROGRAMADOR", "CONDUCTOR", "TECNICO"}) {
            reset(service);
            mvc.perform(get("/api/v1/trabajadores/me").param("correo", "ajeno@example.com")
                .with(user("personal@example.com").roles(rol))).andExpect(status().isOk());
            verify(service).me("personal@example.com"); verifyNoMoreInteractions(service);
        }
    }
    @Test void clienteNoConsultaPerfilDePersonal() throws Exception {
        mvc.perform(get("/api/v1/trabajadores/me").with(user("cliente@example.com").roles("CLIENTE"))).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    @Test void sinSesionNoSeExponeElPerfil() throws Exception {
        mvc.perform(get("/api/v1/trabajadores/me")).andExpect(status().isUnauthorized()); verifyNoInteractions(service);
    }
    @Test void soloAdminPuedeEditarDesactivarYReactivarTrabajadores() throws Exception {
        String body = """
            {"nombres":"Ana","apellidos":"Prueba","dni":"12345678","telefono":"987654321",
             "direccion":"Lima","fechaIngreso":"2026-01-01"}
            """;
        for (String rol : new String[]{"ANALISTA", "PROGRAMADOR", "CONDUCTOR", "TECNICO", "CLIENTE"}) {
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/trabajadores/7")
                .contentType("application/json").content(body).with(user("otro@example.com").roles(rol))).andExpect(status().isForbidden());
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/trabajadores/7")
                .with(user("otro@example.com").roles(rol))).andExpect(status().isForbidden());
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/trabajadores/7/activar")
                .with(user("otro@example.com").roles(rol))).andExpect(status().isForbidden());
        }
        verifyNoInteractions(service);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/trabajadores/7")
            .contentType("application/json").content(body).with(user("admin@example.com").roles("ADMIN"))).andExpect(status().isOk());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/trabajadores/7")
            .with(user("admin@example.com").roles("ADMIN"))).andExpect(status().isOk());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/trabajadores/7/activar")
            .with(user("admin@example.com").roles("ADMIN"))).andExpect(status().isOk());
        verify(service).actualizar(eq(7), any());
        verify(service).cambiarActivo(7, false, "admin@example.com");
        verify(service).cambiarActivo(7, true, "admin@example.com");
    }
}
