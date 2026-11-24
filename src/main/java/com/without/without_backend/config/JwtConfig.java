package com.without.without_backend.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;

@Data
@Slf4j
@Validated
@Configuration
@ConfigurationProperties(prefix = "application.jwt")
public class JwtConfig {
  private String secretKey;
  @Min(1)
  private Integer accessTokenExpirationMinutes = 15;
  @Min(1)
  private Integer refreshTokenExpirationAfterDays = 30;

  public long getTokenExpirationInMillis() {
    return accessTokenExpirationMinutes * 60L * 1000;
  }

  public long getRefreshTokenExpirationInMillis() {
    return refreshTokenExpirationAfterDays * 24L * 60 * 60 * 1000;
  }

  @Bean
  SecretKey secretKey(Environment environment) {
    if (secretKey == null || secretKey.isBlank()) {
      if (environment.matchesProfiles("prod")) {
        throw new IllegalStateException("JWT_SECRET es obligatorio en produccion");
      }
      // Clave efimera local: reiniciar invalida los JWT emitidos anteriormente.
      log.warn("JWT_SECRET no configurado: se usa una clave efimera local; reiniciar invalida las sesiones.");
      return Jwts.SIG.HS256.key().build();
    }
    return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
  }

}
