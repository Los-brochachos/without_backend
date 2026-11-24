package com.without.without_backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import com.without.without_backend.config.JwtConfig;
import com.without.without_backend.services.AuthService;
import com.without.without_backend.utils.auth.AuthRequest;
import com.without.without_backend.utils.auth.AuthResponse;
import com.without.without_backend.utils.auth.AuthTokens;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Autenticación", description = "Login con JWT, renovación de sesión (refresh token en cookie HttpOnly) y logout. Endpoints públicos.")
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthRestController {
  private final AuthService authService;
  private final JwtConfig jwtConfig;
  @Value("${application.auth.cookie-secure:true}") private boolean cookieSecure;
  @Value("${application.auth.cookie-same-site:Strict}") private String sameSite;

  @Operation(summary = "Obtener token CSRF",
      description = "Paso previo obligatorio a login, refresh-token y logout. Devuelve el token que debe enviarse "
          + "en el header indicado por `headerName` (X-XSRF-TOKEN). También deja la cookie CSRF HttpOnly en /auth.")
  @SecurityRequirements
  @ApiResponse(responseCode = "200", description = "Token CSRF generado",
      content = @Content(examples = @ExampleObject("{\"token\":\"a1b2c3...\",\"headerName\":\"X-XSRF-TOKEN\"}")))
  @GetMapping("/csrf")
  public ResponseEntity<?> csrf(@Parameter(hidden = true) CsrfToken token) {
    return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store")
        .body(Map.of("token", token.getToken(), "headerName", token.getHeaderName()));
  }

  @Operation(summary = "Iniciar sesión",
      description = "Valida correo y contraseña. Devuelve el **access token JWT** (15 min) en el cuerpo y el "
          + "**refresh token** (30 días) en la cookie `without_refresh` (HttpOnly, SameSite=Strict, path=/auth). "
          + "Requiere el header CSRF obtenido en GET /auth/csrf.")
  @SecurityRequirements
  @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true,
      description = "Valor de `token` devuelto por GET /auth/csrf")
  @ApiResponse(responseCode = "200", description = "Autenticado",
      content = @Content(schema = @Schema(implementation = AuthResponse.class),
          examples = @ExampleObject("{\"access_token\":\"eyJhbGciOiJIUzI1NiJ9...\"}")))
  @ApiResponse(responseCode = "400", description = "Correo o contraseña con formato inválido", content = @Content)
  @ApiResponse(responseCode = "401", description = "Credenciales inválidas o usuario inactivo",
      content = @Content(examples = @ExampleObject("{\"message\":\"Credenciales invalidas\"}")))
  @ApiResponse(responseCode = "403", description = "Falta o es inválido el token CSRF", content = @Content)
  @PostMapping("/login")
  public ResponseEntity<?> authenticate(@Valid @RequestBody AuthRequest request) {
    try {
      return authenticated(authService.authenticate(request));
    } catch (AuthenticationException ex) {
      return ResponseEntity.status(401).header(HttpHeaders.CACHE_CONTROL, "no-store")
          .body(Map.of("message", "Credenciales invalidas"));
    }
  }

  @PostMapping("/refresh-token")
  @Operation(summary = "Renovar sesión (rotación de refresh token)",
      description = "Usa la cookie HttpOnly `without_refresh` (el navegador la envía sola). Si es válida, la consume, "
          + "emite un nuevo access token y una nueva cookie de refresh. Un refresh token ya usado o revocado responde 401. "
          + "Requiere el header CSRF.")
  @SecurityRequirements
  @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true,
      description = "Valor de `token` devuelto por GET /auth/csrf")
  @ApiResponse(responseCode = "200", description = "Sesión renovada",
      content = @Content(schema = @Schema(implementation = AuthResponse.class)))
  @ApiResponse(responseCode = "401", description = "Cookie ausente, vencida, revocada o reutilizada",
      content = @Content(examples = @ExampleObject("{\"message\":\"Sesion invalida\"}")))
  public ResponseEntity<?> refreshToken(@Parameter(hidden = true) @CookieValue(name = "without_refresh", required = false) String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) return invalidSession();
    try {
      return authenticated(authService.refreshToken(refreshToken));
    } catch (AuthenticationException ex) {
      return invalidSession();
    }
  }

  @Operation(summary = "Cerrar sesión",
      description = "Revoca el refresh token en base de datos y borra la cookie `without_refresh`. "
          + "Requiere el header CSRF. El access token en memoria del cliente debe descartarse.")
  @SecurityRequirements
  @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true,
      description = "Valor de `token` devuelto por GET /auth/csrf")
  @ApiResponse(responseCode = "204", description = "Sesión cerrada y cookie eliminada")
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@Parameter(hidden = true) @CookieValue(name = "without_refresh", required = false) String refreshToken) {
    authService.logout(refreshToken);
    return ResponseEntity.noContent().header(HttpHeaders.CACHE_CONTROL, "no-store")
        .header(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString()).build();
  }

  private ResponseEntity<AuthResponse> authenticated(AuthTokens tokens) {
    return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store")
        .header(HttpHeaders.SET_COOKIE, cookie(tokens.refreshToken(),
            Duration.ofDays(jwtConfig.getRefreshTokenExpirationAfterDays())).toString())
        .body(new AuthResponse(tokens.accessToken()));
  }

  private ResponseEntity<?> invalidSession() {
    return ResponseEntity.status(401).header(HttpHeaders.CACHE_CONTROL, "no-store")
        .header(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString())
        .body(Map.of("message", "Sesion invalida"));
  }

  private ResponseCookie cookie(String value, Duration age) {
    return ResponseCookie.from("without_refresh", value).httpOnly(true).secure(cookieSecure)
        .sameSite(sameSite).path("/auth").maxAge(age).build();
  }
}