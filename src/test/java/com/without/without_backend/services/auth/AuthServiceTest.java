package com.without.without_backend.services.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.repositories.usuario.UsuarioRepository;
import com.without.without_backend.services.*;
import com.without.without_backend.utils.Estado;
import com.without.without_backend.utils.auth.AuthRequest;

class AuthServiceTest {
    private TokenService tokens;
    private UsuarioRepository users;
    private JwtService jwt;
    private AuthenticationManager manager;
    private AuthService service;
    private Usuario user;
    @BeforeEach void setup() {
        tokens = mock(TokenService.class); users = mock(UsuarioRepository.class);
        jwt = mock(JwtService.class); manager = mock(AuthenticationManager.class);
        service = new AuthService(tokens, users, jwt, manager);
        user = Usuario.builder().idUsuario(1).correo("cliente@without.example").estado(Estado.ACTIVO).build();
    }
    @Test void loginGeneraAmbosTokensPeroSoloPersisteRefresh() {
        when(users.findByCorreo(user.getCorreo())).thenReturn(Optional.of(user));
        when(jwt.generateToken(user)).thenReturn("access");
        when(jwt.generateRefreshToken(user)).thenReturn("refresh");
        var response = service.authenticate(new AuthRequest(user.getCorreo(), "clave"));
        assertEquals("access", response.accessToken());
        assertEquals("refresh", response.refreshToken());
        verify(manager).authenticate(any());
        verify(tokens).saveRefreshToken(user, "refresh");
    }
    @Test void renovarConsumeElAnteriorYGuardaUnoNuevo() {
        when(jwt.extractUsername("old")).thenReturn(user.getCorreo());
        when(users.findByCorreo(user.getCorreo())).thenReturn(Optional.of(user));
        when(jwt.isRefreshTokenValid("old", user)).thenReturn(true);
        when(tokens.consumeRefreshToken("old")).thenReturn(user);
        when(jwt.generateToken(user)).thenReturn("access");
        when(jwt.generateRefreshToken(user)).thenReturn("new");
        assertEquals("new", service.refreshToken("old").refreshToken());
        var order = inOrder(tokens);
        order.verify(tokens).consumeRefreshToken("old");
        order.verify(tokens).saveRefreshToken(user, "new");
    }
    @Test void accessNoRenuevaAunqueElCorreoSeaValido() {
        when(jwt.extractUsername("access")).thenReturn(user.getCorreo());
        when(users.findByCorreo(user.getCorreo())).thenReturn(Optional.of(user));
        when(jwt.isRefreshTokenValid("access", user)).thenReturn(false);
        assertThrows(BadCredentialsException.class, () -> service.refreshToken("access"));
        verifyNoInteractions(tokens);
    }
}
