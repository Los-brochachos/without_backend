package com.without.without_backend.controllers.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import com.jayway.jsonpath.JsonPath;
import com.without.without_backend.components.JwtAuthFilter;
import com.without.without_backend.config.JwtConfig;
import com.without.without_backend.config.SecurityConfig;
import com.without.without_backend.controllers.AuthRestController;
import com.without.without_backend.services.AuthService;
import com.without.without_backend.services.JwtService;
import com.without.without_backend.utils.auth.AuthTokens;
import jakarta.servlet.http.Cookie;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = AuthSecurityTest.Config.class)
@TestPropertySource(properties = "application.auth.cookie-secure=true")
class AuthSecurityTest {
    @Configuration @EnableWebMvc
    @Import({SecurityConfig.class, AuthRestController.class, JwtAuthFilter.class})
    static class Config {
        @Bean AuthService authService() { return mock(AuthService.class); }
        @Bean JwtService jwtService() { return mock(JwtService.class); }
        @Bean UserDetailsService userDetailsService() { return mock(UserDetailsService.class); }
        @Bean JwtConfig jwtConfig() {
            JwtConfig config = new JwtConfig(); config.setRefreshTokenExpirationAfterDays(30); return config;
        }
    }
    @Autowired private WebApplicationContext context;
    @Autowired private AuthService auth;
    @Autowired private JwtService jwt;
    private MockMvc mvc;
    @BeforeEach void setup() {
        reset(auth, jwt);
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test void loginSinCsrfSeRechaza() throws Exception {
        mvc.perform(post("/auth/login").contentType("application/json")
            .content("{\"correo\":\"cliente@without.example\",\"contraseña\":\"clave\"}"))
            .andExpect(status().isForbidden());
        verifyNoInteractions(auth);
    }
    @Test void loginConCsrfRealDevuelveCookieProtegidaSinRefreshEnJson() throws Exception {
        var csrf = csrf();
        when(auth.authenticate(any())).thenReturn(new AuthTokens("access", "refresh"));
        mvc.perform(post("/auth/login").cookie(csrf.cookie()).header(csrf.header(), csrf.value())
            .contentType("application/json").content("{\"correo\":\"cliente@without.example\",\"contraseña\":\"clave\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.access_token").value("access"))
            .andExpect(jsonPath("$.refresh_token").doesNotExist())
            .andExpect(cookie().httpOnly("without_refresh", true))
            .andExpect(cookie().secure("without_refresh", true))
            .andExpect(cookie().path("without_refresh", "/auth"))
            .andExpect(header().string("Cache-Control", "no-store"))
            .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("SameSite=Strict")));
    }
    @Test void refreshSinCookieNoAceptaElTokenEnElCuerpo() throws Exception {
        var csrf = csrf();
        mvc.perform(post("/auth/refresh-token").cookie(csrf.cookie()).header(csrf.header(), csrf.value())
            .contentType("application/json").content("{\"refreshToken\":\"old\"}"))
            .andExpect(status().isUnauthorized()).andExpect(cookie().maxAge("without_refresh", 0));
        verifyNoInteractions(auth);
    }
    @Test void refreshConCookieRotadaDevuelveSoloAccess() throws Exception {
        var csrf = csrf();
        when(auth.refreshToken("old")).thenReturn(new AuthTokens("access-new", "refresh-new"));
        mvc.perform(post("/auth/refresh-token").cookie(csrf.cookie(), new Cookie("without_refresh", "old"))
            .header(csrf.header(), csrf.value())).andExpect(status().isOk())
            .andExpect(jsonPath("$.access_token").value("access-new"))
            .andExpect(jsonPath("$.refresh_token").doesNotExist())
            .andExpect(cookie().value("without_refresh", "refresh-new"));
    }
    @Test void refreshRevocadoLimpiaCookieYDevuelve401() throws Exception {
        var csrf = csrf();
        when(auth.refreshToken("old")).thenThrow(new BadCredentialsException("internal detail"));
        mvc.perform(post("/auth/refresh-token").cookie(csrf.cookie(), new Cookie("without_refresh", "old"))
            .header(csrf.header(), csrf.value())).andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Sesion invalida"))
            .andExpect(cookie().maxAge("without_refresh", 0));
    }
    @Test void logoutRevocaYLimpiaCookie() throws Exception {
        var csrf = csrf();
        mvc.perform(post("/auth/logout").cookie(csrf.cookie(), new Cookie("without_refresh", "old"))
            .header(csrf.header(), csrf.value())).andExpect(status().isNoContent())
            .andExpect(cookie().maxAge("without_refresh", 0));
        verify(auth).logout("old");
    }
    @Test void refreshSinCsrfSeRechazaAunqueTengaCookie() throws Exception {
        mvc.perform(post("/auth/refresh-token").cookie(new Cookie("without_refresh", "old")))
            .andExpect(status().isForbidden());
        verifyNoInteractions(auth);
    }
    @Test void apiSinBearerDevuelve401() throws Exception {
        mvc.perform(get("/api/v1/usuarios/me")).andExpect(status().isUnauthorized());
    }
    @Test void filtroRechazaRefreshComoBearer() throws Exception {
        when(jwt.extractUsername("refresh")).thenReturn("cliente@without.example");
        var details = context.getBean(UserDetailsService.class);
        when(details.loadUserByUsername("cliente@without.example"))
            .thenReturn(org.springframework.security.core.userdetails.User.withUsername("cliente@without.example")
                .password("hash").roles("CLIENTE").build());
        mvc.perform(get("/api/v1/usuarios/me").header("Authorization", "Bearer refresh"))
            .andExpect(status().isUnauthorized());
    }
    private Csrf csrf() throws Exception {
        var result = mvc.perform(get("/auth/csrf")).andExpect(status().isOk()).andReturn();
        Cookie cookie = result.getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(cookie);
        String json = result.getResponse().getContentAsString();
        return new Csrf(cookie, JsonPath.read(json, "$.headerName"), JsonPath.read(json, "$.token"));
    }
    record Csrf(Cookie cookie, String header, String value) {}
}
