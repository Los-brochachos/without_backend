package com.without.without_backend.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.without.without_backend.repositories.usuario.UsuarioRepository;
import com.without.without_backend.utils.auth.AuthRequest;
import com.without.without_backend.utils.auth.AuthTokens;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final TokenService tokenService;
  private final UsuarioRepository userRepository;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  @Transactional
  public AuthTokens authenticate(AuthRequest request) {
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.correo(), request.contraseña()));
    var user = userRepository.findByCorreo(request.correo())
        .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));
    if (!user.isEnabled()) throw new BadCredentialsException("Credenciales invalidas");
    return issue(user);
  }

  @Transactional
  public AuthTokens refreshToken(String refreshToken) {
    try {
      String email = jwtService.extractUsername(refreshToken);
      var user = userRepository.findByCorreo(email)
          .orElseThrow(() -> new BadCredentialsException("Sesion invalida"));
      if (!jwtService.isRefreshTokenValid(refreshToken, user)) throw new BadCredentialsException("Sesion invalida");
      var owner = tokenService.consumeRefreshToken(refreshToken);
      if (!user.getIdUsuario().equals(owner.getIdUsuario())) throw new BadCredentialsException("Sesion invalida");
      return issue(user);
    } catch (JwtException | IllegalArgumentException ex) {
      throw new BadCredentialsException("Sesion invalida", ex);
    }
  }

  @Transactional
  public void logout(String refreshToken) {
    tokenService.revokeRefreshToken(refreshToken);
  }

  private AuthTokens issue(com.without.without_backend.entities.usuario.Usuario user) {
    String access = jwtService.generateToken(user);
    String refresh = jwtService.generateRefreshToken(user);
    tokenService.saveRefreshToken(user, refresh);
    return new AuthTokens(access, refresh);
  }
}