package com.without.without_backend.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.without.without_backend.config.JwtConfig;
import com.without.without_backend.entities.usuario.Token;
import com.without.without_backend.entities.usuario.Token.TokenType;
import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.repositories.TokenRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenService {
  private final TokenRepository repository;
  private final JwtConfig jwtConfig;

  public List<Token> getTokensByUser(Usuario usuario) {
    return repository.findByUser(usuario).orElse(List.of());
  }

  @Transactional
  public void saveRefreshToken(Usuario usuario, String refreshToken) {
    repository.save(Token.builder().user(usuario).token(hash(refreshToken))
        .type(TokenType.REFRESH_TOKEN)
        .expirationDate(LocalDateTime.now().plusDays(jwtConfig.getRefreshTokenExpirationAfterDays())).build());
  }

  /** El bloqueo y la transaccion de AuthService evitan consumir el mismo refresh dos veces. */
  @Transactional
  public Usuario consumeRefreshToken(String refreshToken) {
    Token stored = repository.findByToken(hash(refreshToken))
        .orElseThrow(() -> new BadCredentialsException("Sesion invalida"));
    if (stored.getType() != TokenType.REFRESH_TOKEN || stored.getUser() == null
        || !stored.getExpirationDate().isAfter(LocalDateTime.now()) || !stored.getUser().isEnabled()) {
      throw new BadCredentialsException("Sesion invalida");
    }
    Usuario user = stored.getUser();
    repository.delete(stored);
    return user;
  }

  @Transactional
  public void revokeRefreshToken(String refreshToken) {
    if (refreshToken != null && !refreshToken.isBlank()) {
      repository.findByToken(hash(refreshToken)).ifPresent(repository::delete);
    }
  }

  static String hash(String token) {
    if (token == null || token.isBlank()) throw new BadCredentialsException("Sesion invalida");
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
          .digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException(ex);
    }
  }
}