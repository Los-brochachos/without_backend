package com.without.without_backend.services;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.without.without_backend.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {
  private final JwtConfig jwtConfig;
  private final SecretKey secretKey;

  public String generateToken(UserDetails userDetails) {
    return generateToken(new HashMap<>(), userDetails);
  }

  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return buildToken(extraClaims, userDetails, jwtConfig.getTokenExpirationInMillis(), "access");
  }

  public String generateRefreshToken(UserDetails userDetails) {
    return buildToken(new HashMap<>(), userDetails, jwtConfig.getRefreshTokenExpirationInMillis(), "refresh");
  }

  private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expirationMillis, String type) {
    
    return Jwts
        .builder()
        .claims(extraClaims)
        .subject(userDetails.getUsername())
        .id(UUID.randomUUID().toString())
        .claim("token_use", type)
        .claim("role", userDetails.getAuthorities().stream()
          .findFirst()
          .map(authority -> authority.getAuthority())
          .orElse(""))
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + expirationMillis))
        .signWith(secretKey)
        .compact();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    return validFor(token, userDetails, "access");
  }

  public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
    return validFor(token, userDetails, "refresh");
  }

  private boolean validFor(String token, UserDetails userDetails, String use) {
    Claims claims = extractAllClaims(token);
    return userDetails.isEnabled() && userDetails.isAccountNonLocked()
        && userDetails.getUsername().equals(claims.getSubject())
        && use.equals(claims.get("token_use", String.class))
        && claims.getExpiration() != null && claims.getExpiration().after(new Date());
  }

  public String extractUsername(String token) {
    return extractClaim(token, claims -> claims.getSubject());
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    try {
      return Jwts
          .parser()
          .verifyWith(secretKey)
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (ExpiredJwtException e) {
      throw new JwtException("Token has expired", e);
    } catch (JwtException | IllegalArgumentException e) {
      throw new JwtException("Token not valid", e);
    }
  }
}
