package com.without.without_backend.services.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.BadCredentialsException;
import com.without.without_backend.config.JwtConfig;
import com.without.without_backend.entities.usuario.Token;
import com.without.without_backend.entities.usuario.Token.TokenType;
import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.repositories.TokenRepository;
import com.without.without_backend.services.TokenService;
import com.without.without_backend.utils.Estado;

class TokenServiceTest {
    private TokenRepository repository;
    private TokenService service;
    private Usuario user;
    @BeforeEach void setup() {
        repository = mock(TokenRepository.class);
        JwtConfig config = new JwtConfig(); config.setRefreshTokenExpirationAfterDays(30);
        service = new TokenService(repository, config);
        user = Usuario.builder().idUsuario(1).estado(Estado.ACTIVO).build();
    }
    @Test void guardaHashDelRefreshYNoSuValorOriginal() {
        service.saveRefreshToken(user, "refresh-secreto");
        var captor = ArgumentCaptor.forClass(Token.class);
        verify(repository).save(captor.capture());
        assertNotEquals("refresh-secreto", captor.getValue().getToken());
        assertEquals(64, captor.getValue().getToken().length());
        assertEquals(TokenType.REFRESH_TOKEN, captor.getValue().getType());
    }
    @Test void consumeSoloUnaVezYRechazaReutilizacion() {
        Token token = Token.builder().user(user).type(TokenType.REFRESH_TOKEN)
            .expirationDate(LocalDateTime.now().plusDays(1)).build();
        when(repository.findByToken(anyString()))
            .thenReturn(Optional.of(token))
            .thenReturn(Optional.empty());
        assertSame(user, service.consumeRefreshToken("refresh"));
        verify(repository).delete(token);
        assertThrows(BadCredentialsException.class, () -> service.consumeRefreshToken("refresh"));
    }
    @Test void refreshVencidoNoSeConsume() {
        Token token = Token.builder().user(user).type(TokenType.REFRESH_TOKEN)
            .expirationDate(LocalDateTime.now().minusSeconds(1)).build();
        when(repository.findByToken(anyString())).thenReturn(Optional.of(token));
        assertThrows(BadCredentialsException.class, () -> service.consumeRefreshToken("refresh"));
        verify(repository, never()).delete(any());
    }
    @Test void logoutBorraElRefreshRegistrado() {
        Token token = Token.builder().build();
        when(repository.findByToken(anyString())).thenReturn(Optional.of(token));
        service.revokeRefreshToken("refresh");
        verify(repository).delete(token);
    }
    @Test void logoutSinCookieEsIdempotente() {
        service.revokeRefreshToken(null);
        verifyNoInteractions(repository);
    }
}
