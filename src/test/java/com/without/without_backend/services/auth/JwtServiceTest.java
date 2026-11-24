package com.without.without_backend.services.auth;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.without.without_backend.config.JwtConfig;
import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.services.JwtService;
import com.without.without_backend.utils.Estado;
import com.without.without_backend.utils.RolesEnum;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;

class JwtServiceTest {
    private JwtService jwt;
    private JwtConfig config;
    private Usuario user;
    @BeforeEach void setup() {
        config = new JwtConfig();
        config.setRefreshTokenExpirationAfterDays(30);
        jwt = new JwtService(config, Jwts.SIG.HS256.key().build());
        user = Usuario.builder().correo("cliente@without.example").rol(RolesEnum.CLIENTE).estado(Estado.ACTIVO).build();
    }
    @Test void accessTieneDuracionCortaYCorreo() {
        String token = jwt.generateToken(user);
        assertTrue(jwt.isTokenValid(token, user));
        assertEquals(user.getCorreo(), jwt.extractUsername(token));
        assertEquals("ROLE_CLIENTE", jwt.extractClaim(token, c -> c.get("role", String.class)));
        long duration = jwt.extractClaim(token, c -> c.getExpiration().getTime() - c.getIssuedAt().getTime());
        assertEquals(900000L, duration);
    }
    @Test void refreshNoPuedeAutenticarUnaApi() {
        String token = jwt.generateRefreshToken(user);
        assertFalse(jwt.isTokenValid(token, user));
        assertTrue(jwt.isRefreshTokenValid(token, user));
    }
    @Test void accessNoPuedeRenovarUnaSesion() {
        assertFalse(jwt.isRefreshTokenValid(jwt.generateToken(user), user));
    }
    @Test void tokensEmitidosEnElMismoInstanteSonDistintos() {
        assertNotEquals(jwt.generateRefreshToken(user), jwt.generateRefreshToken(user));
    }
    @Test void usuarioInactivoNoPuedeUsarSusTokens() {
        String token = jwt.generateToken(user);
        user.setEstado(Estado.INACTIVO);
        assertFalse(jwt.isTokenValid(token, user));
    }
    @Test void tokenVencidoSeRechaza() {
        config.setAccessTokenExpirationMinutes(-1);
        String token = jwt.generateToken(user);
        assertThrows(JwtException.class, () -> jwt.isTokenValid(token, user));
    }
    @Test void firmaDeOtraClaveSeRechaza() {
        String token = jwt.generateToken(user);
        JwtService other = new JwtService(config, Jwts.SIG.HS256.key().build());
        assertThrows(JwtException.class, () -> other.isTokenValid(token, user));
    }
}
