package com.without.without_backend.components;

import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.without.without_backend.services.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return request.getRequestURI().substring(request.getContextPath().length()).startsWith("/auth/");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      try {
        String jwt = header.substring(7);
        String email = jwtService.extractUsername(jwt);
        if (email == null) throw new JwtException("Invalid subject");
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
          var user = userDetailsService.loadUserByUsername(email);
          if (!jwtService.isTokenValid(jwt, user)) throw new JwtException("Invalid access token");
          var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      } catch (JwtException | AuthenticationException | IllegalArgumentException ex) {
        SecurityContextHolder.clearContext();
        response.setStatus(401);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"Sesion invalida\"}");
        return;
      }
    }
    // No capturar excepciones de controladores como si fueran fallos de autenticacion.
    filterChain.doFilter(request, response);
  }
}
