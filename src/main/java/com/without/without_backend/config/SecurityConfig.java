package com.without.without_backend.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.without.without_backend.components.JwtAuthFilter;

import jakarta.servlet.DispatcherType;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity 
@RequiredArgsConstructor 
 
public class SecurityConfig {
    private final JwtAuthFilter filter;
    @Value("${application.auth.cookie-secure:true}")
    private boolean cookieSecure;
    @Value("${application.auth.allowed-origins:http://127.0.0.1:3000,http://localhost:4200,http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository())
                .requireCsrfProtectionMatcher(request ->
                    request.getRequestURI().substring(request.getContextPath().length()).startsWith("/auth/")
                    && !Arrays.asList("GET", "HEAD", "OPTIONS", "TRACE").contains(request.getMethod())))
            .cors(Customizer.withDefaults())
            .logout(logout -> logout.disable())
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, exception) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"Autenticacion requerida\"}");
                })
                // Responder 403 directamente. El handler por defecto hace sendError(403) y Tomcat
                // reenvía a /error, donde el JWT ya no se evalúa: el cliente recibía 401 en vez de 403.
                .accessDeniedHandler((request, response, exception) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"No tienes permiso para realizar esta accion\"}");
                }))
            .authorizeHttpRequests(auth -> auth

                // Reenvíos internos de error (sendError) no deben volver a exigir autenticación.
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                .requestMatchers(HttpMethod.PATCH, "/api/v1/cuenta/clave").authenticated()

                //ENDPOINTS PUBLICOS - AUTENTICACIÓN
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/refresh-token").permitAll()
                .requestMatchers(HttpMethod.GET, "/auth/csrf").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/logout").permitAll()
                .requestMatchers("/prueba/**").permitAll()

                // DOCUMENTACION SWAGGER / OPENAPI (solo lectura; se desactiva en prod)
                .requestMatchers(HttpMethod.GET, "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**")
                    .permitAll()


                // ========================================================
                // USUARIOS
                // Consultar mi usuario
                .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/me")
                    .authenticated()
                // Actualizar mi propio correo
                .requestMatchers(HttpMethod.PATCH, "/api/v1/usuarios/me")
                    .authenticated()
                // Listar usuarios
                .requestMatchers(HttpMethod.GET, "/api/v1/usuarios")
                    .hasAnyRole("ADMIN", "ANALISTA")
                // Consultar usuario por ID
                .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/*")
                    .hasAnyRole("ADMIN", "ANALISTA")
                // Actualizar usuario por ID
                .requestMatchers(HttpMethod.PATCH, "/api/v1/usuarios/*")
                    .hasRole("ADMIN")
                // Desactivar usuario
                .requestMatchers(HttpMethod.DELETE, "/api/v1/usuarios/*")
                    .hasRole("ADMIN")
                // =========================================================
                
                
                
                // =========================================================
                // CONDUCTORES
                // Listar todos los conductores
                .requestMatchers(HttpMethod.GET, "/api/v1/conductores/me")
                    .hasRole("CONDUCTOR")

                .requestMatchers(HttpMethod.GET, "/api/v1/conductores")
                    .hasAnyRole("ADMIN", "ANALISTA")

                // Listar conductores activos
                .requestMatchers(HttpMethod.GET, "/api/v1/conductores/activos")
                    .hasAnyRole("ADMIN", "ANALISTA", "PROGRAMADOR")

                // Buscar conductor por ID
                .requestMatchers(HttpMethod.GET, "/api/v1/conductores/*")
                    .hasAnyRole("ADMIN", "ANALISTA")

                // Buscar conductor activo por ID
                .requestMatchers(HttpMethod.GET, "/api/v1/conductores/*/activo")
                    .hasAnyRole("ADMIN", "ANALISTA", "PROGRAMADOR")

                // Crear conductor
                .requestMatchers(HttpMethod.POST, "/api/v1/conductores")
                    .hasRole("ADMIN")

                // Actualizar conductor
                .requestMatchers(HttpMethod.PUT, "/api/v1/conductores/*")
                    .hasRole("ADMIN")

                // Desactivar conductor
                .requestMatchers(HttpMethod.DELETE, "/api/v1/conductores/*")
                    .hasRole("ADMIN")          
                // =========================================================
                

                // =========================================================
                // PROGRAMADORES
                // Obtener mis propios datos
                .requestMatchers(HttpMethod.GET, "/api/v1/programadores/me")
                    .hasRole("PROGRAMADOR")

                // Listar todos los programadores
                .requestMatchers(HttpMethod.GET, "/api/v1/programadores")
                    .hasAnyRole("ADMIN", "ANALISTA")

                // Listar programadores activos
                .requestMatchers(HttpMethod.GET, "/api/v1/programadores/activos")
                    .hasAnyRole("ADMIN", "ANALISTA")

                // Buscar programador por ID
                .requestMatchers(HttpMethod.GET, "/api/v1/programadores/*")
                    .hasAnyRole("ADMIN", "ANALISTA")

                // Buscar programador activo por ID
                .requestMatchers(HttpMethod.GET, "/api/v1/programadores/*/activo")
                    .hasAnyRole("ADMIN", "ANALISTA")

                // Crear programador
                .requestMatchers(HttpMethod.POST, "/api/v1/programadores")
                    .hasRole("ADMIN")

                // Actualizar programador
                .requestMatchers(HttpMethod.PUT, "/api/v1/programadores/*")
                    .hasRole("ADMIN")

                // Desactivar programador
                .requestMatchers(HttpMethod.DELETE, "/api/v1/programadores/*")
                    .hasRole("ADMIN")

                // =========================================================
                                

                // =========================================================
                // TÉCNICOS
                // Obtener mis propios datos
                .requestMatchers(HttpMethod.GET, "/api/v1/tecnicos/me")
                    .hasRole("TECNICO")

                // Listar todos los técnicos
                .requestMatchers(HttpMethod.GET, "/api/v1/tecnicos")
                    .hasAnyRole("ADMIN", "ANALISTA")

                // Listar técnicos activos
                .requestMatchers(HttpMethod.GET, "/api/v1/tecnicos/activos")
                    .hasAnyRole("ADMIN", "ANALISTA")

                // Buscar técnico por ID
                .requestMatchers(HttpMethod.GET, "/api/v1/tecnicos/*")
                    .hasAnyRole("ADMIN", "ANALISTA")

                // Buscar técnico activo por ID
                .requestMatchers(HttpMethod.GET, "/api/v1/tecnicos/*/activo")
                    .hasAnyRole("ADMIN", "ANALISTA")

                // Crear técnico
                .requestMatchers(HttpMethod.POST, "/api/v1/tecnicos")
                    .hasRole("ADMIN")

                // Actualizar técnico
                .requestMatchers(HttpMethod.PUT, "/api/v1/tecnicos/*")
                    .hasRole("ADMIN")

                // Desactivar técnico
                .requestMatchers(HttpMethod.DELETE, "/api/v1/tecnicos/*")
                    .hasRole("ADMIN")
                // =========================================================
            
                // =========================================================
                // TRABAJADORES

                .requestMatchers(HttpMethod.GET, "/api/v1/trabajadores/me")
                    .hasAnyRole("ADMIN", "ANALISTA", "PROGRAMADOR", "CONDUCTOR", "TECNICO")

                .requestMatchers(HttpMethod.POST, "/api/v1/trabajadores")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/trabajadores/*")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/trabajadores/*")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/trabajadores/*/activar")
                    .hasRole("ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/v1/trabajadores")
                    .hasAnyRole("ADMIN", "ANALISTA")

                .requestMatchers(HttpMethod.GET, "/api/v1/trabajadores/activos")
                    .hasAnyRole("ADMIN", "ANALISTA")

                .requestMatchers(HttpMethod.GET, "/api/v1/trabajadores/dni/*/activo")
                    .hasAnyRole("ADMIN", "ANALISTA")

                .requestMatchers(HttpMethod.GET, "/api/v1/trabajadores/dni/*")
                    .hasAnyRole("ADMIN", "ANALISTA")

                .requestMatchers(HttpMethod.GET, "/api/v1/trabajadores/*/activo")
                    .hasAnyRole("ADMIN", "ANALISTA")

                .requestMatchers(HttpMethod.GET, "/api/v1/trabajadores/*")
                    .hasAnyRole("ADMIN", "ANALISTA")
                // =========================================================




                // =========================================================
                // CLIENTES
                // Registrar cliente
                .requestMatchers(HttpMethod.POST, "/api/v1/clientes/register")
                    .permitAll()

                // Obtener mis propios datos
                .requestMatchers(HttpMethod.GET, "/api/v1/clientes/me")
                    .hasRole("CLIENTE")

                // Actualizar mis propios datos
                .requestMatchers(HttpMethod.PATCH, "/api/v1/clientes/me")
                    .hasRole("CLIENTE")

                // Listar todos los clientes
                .requestMatchers(HttpMethod.GET, "/api/v1/clientes")
                    .hasAnyRole("ADMIN", "ANALISTA")

                // Listar clientes activos
                .requestMatchers(HttpMethod.GET, "/api/v1/clientes/activos")
                    .hasAnyRole("ADMIN", "ANALISTA")

                // Buscar cliente por ID
                .requestMatchers(HttpMethod.GET, "/api/v1/clientes/*")
                    .hasAnyRole("ADMIN", "ANALISTA")

                // Buscar cliente activo por ID
                .requestMatchers(HttpMethod.GET, "/api/v1/clientes/*/activo")
                    .hasAnyRole("ADMIN", "ANALISTA")

                // Actualizar cliente por ID
                .requestMatchers(HttpMethod.PUT, "/api/v1/clientes/*")
                    .hasRole("ADMIN")

                // Desactivar cliente
                .requestMatchers(HttpMethod.DELETE, "/api/v1/clientes/*")
                    .hasRole("ADMIN")
                // =========================================================



                // =========================================================
                // PEDIDOS
                // Paginados (deben ir antes de los patrones /* para que no los capturen)
                .requestMatchers(HttpMethod.GET, "/api/v1/pedidos/mis-pedidos/paginado")
                    .hasRole("CLIENTE")
                .requestMatchers(HttpMethod.GET, "/api/v1/pedidos/paginado")
                    .hasAnyRole("ADMIN", "ANALISTA", "PROGRAMADOR")

                // CLIENTE: consultar sus propios pedidos
                .requestMatchers(HttpMethod.GET, "/api/v1/pedidos/mis-pedidos")
                    .hasRole("CLIENTE")

                .requestMatchers(HttpMethod.GET, "/api/v1/pedidos/mis-pedidos/*")
                    .hasRole("CLIENTE")

                // CLIENTE: cancelar su propio pedido (antes caía en anyRequest().authenticated())
                .requestMatchers(HttpMethod.PATCH, "/api/v1/pedidos/mis-pedidos/*/cancelar")
                    .hasRole("CLIENTE")

                // CLIENTE: registrar un pedido
                .requestMatchers(HttpMethod.POST, "/api/v1/pedidos")
                    .hasRole("CLIENTE")

                // ADMIN / ANALISTA / PROGRAMADOR: consultar todos los pedidos
                .requestMatchers(HttpMethod.GET, "/api/v1/pedidos")
                    .hasAnyRole("ADMIN", "ANALISTA", "PROGRAMADOR")

                // ADMIN / ANALISTA / PROGRAMADOR: consultar un pedido específico
                .requestMatchers(HttpMethod.GET, "/api/v1/pedidos/*")
                    .hasAnyRole("ADMIN", "ANALISTA", "PROGRAMADOR")

                // PROGRAMADOR: cambiar estado
                .requestMatchers(HttpMethod.PATCH, "/api/v1/pedidos/*/estado")
                    .hasRole("PROGRAMADOR")

                // ADMIN / PROGRAMADOR: cancelar pedido
                .requestMatchers(HttpMethod.PATCH, "/api/v1/pedidos/*/cancelar")
                    .hasAnyRole("ADMIN", "PROGRAMADOR")
                // =========================================================



                // =========================================================
                // DETALLE PEDIDO
                // CLIENTE: ver los detalles activos de su propio pedido
                .requestMatchers(HttpMethod.GET,"/api/v1/pedidos/*/detalles")
                    .hasRole("CLIENTE")

                // ADMIN / ANALISTA / PROGRAMADOR:
                // ver los detalles activos de cualquier pedido
                .requestMatchers(HttpMethod.GET,"/api/v1/pedidos/*/detalles/activos")
                    .hasAnyRole("ADMIN", "ANALISTA", "PROGRAMADOR")

                // ADMIN / ANALISTA / PROGRAMADOR:
                // ver todos los detalles, incluidos los inactivos
                .requestMatchers(HttpMethod.GET,"/api/v1/pedidos/*/detalles/todos")
                    .hasAnyRole("ADMIN", "ANALISTA", "PROGRAMADOR")

                // CLIENTE: agregar un detalle a su propio pedido
                .requestMatchers(HttpMethod.POST,"/api/v1/pedidos/*/detalles")
                    .hasRole("CLIENTE")

                // CLIENTE: aumentar cantidad de un detalle de su propio pedido
                .requestMatchers(HttpMethod.PATCH,"/api/v1/pedidos/*/detalles/*/cantidad")
                    .hasRole("CLIENTE")

                // CLIENTE: cancelar un detalle de su propio pedido
                .requestMatchers(HttpMethod.PATCH,"/api/v1/pedidos/*/detalles/*/cancelar")
                    .hasRole("CLIENTE")
                // =========================================================


                // =========================================================
                // PRODUCTO
                // ADMIN / ANALISTA:
                // listar todos los productos
                .requestMatchers(HttpMethod.GET,"/api/v1/productos")
                    .hasAnyRole("ADMIN", "ANALISTA")

                // ADMIN / ANALISTA / PROGRAMADOR / CLIENTE:
                // listar productos activos disponibles
                .requestMatchers(HttpMethod.GET,"/api/v1/productos/activos")
                    .hasAnyRole("ADMIN", "ANALISTA", "PROGRAMADOR", "CLIENTE")

                // ADMIN / ANALISTA:
                // buscar producto por ID
                .requestMatchers(HttpMethod.GET,"/api/v1/productos/*")
                    .hasAnyRole("ADMIN", "ANALISTA")

                // ADMIN / ANALISTA / PROGRAMADOR / CLIENTE:
                // buscar producto activo por ID
                .requestMatchers(HttpMethod.GET,"/api/v1/productos/*/activo")
                    .hasAnyRole("ADMIN", "ANALISTA", "PROGRAMADOR", "CLIENTE")

                // ADMIN:
                // crear producto
                .requestMatchers(HttpMethod.POST,"/api/v1/productos")
                    .hasRole("ADMIN")

                // ADMIN:
                // actualizar producto
                .requestMatchers(HttpMethod.PUT,"/api/v1/productos/*")
                    .hasRole("ADMIN")

                // ADMIN:
                // desactivar producto
                .requestMatchers(HttpMethod.DELETE,"/api/v1/productos/*")
                    .hasRole("ADMIN")
                // =========================================================

       

                // =========================================================
                // SEGUIMIENTO PEDIDO
                // CLIENTE: consultar seguimiento de su propio pedido
                .requestMatchers(HttpMethod.GET,"/api/v1/pedidos/*/seguimiento")
                    .hasRole("CLIENTE")

                // ADMIN / ANALISTA / PROGRAMADOR:
                // consultar seguimiento de cualquier pedido
                .requestMatchers(HttpMethod.GET,"/api/v1/pedidos/*/seguimiento/todos")
                    .hasAnyRole("ADMIN", "ANALISTA", "PROGRAMADOR")
                // =========================================================
                                

                // =========================================================
                // CISTERNA
                .requestMatchers(HttpMethod.GET,"/api/v1/cisternas")
                    .hasAnyRole("ADMIN", "ANALISTA")

                .requestMatchers(HttpMethod.GET,"/api/v1/cisternas/activas")
                    .hasAnyRole("ADMIN", "ANALISTA", "PROGRAMADOR", "TECNICO")

                .requestMatchers(HttpMethod.GET,"/api/v1/cisternas/placa/*")
                    .hasAnyRole("ADMIN", "ANALISTA")

                .requestMatchers(HttpMethod.GET,"/api/v1/cisternas/nombre/*")
                    .hasAnyRole("ADMIN", "ANALISTA")

                .requestMatchers(HttpMethod.GET,"/api/v1/cisternas/*/activa")
                    .hasAnyRole("ADMIN", "ANALISTA", "PROGRAMADOR", "TECNICO")

                .requestMatchers(HttpMethod.GET,"/api/v1/cisternas/*")
                    .hasAnyRole("ADMIN", "ANALISTA")

                .requestMatchers(HttpMethod.POST,"/api/v1/cisternas")
                    .hasRole("ADMIN")

                .requestMatchers(HttpMethod.PUT,"/api/v1/cisternas")
                    .hasRole("ADMIN")

                .requestMatchers(HttpMethod.DELETE,"/api/v1/cisternas")
                    .hasRole("ADMIN")
                // =========================================================


                // =========================================================
                // FALLA
                .requestMatchers(HttpMethod.GET, "/api/v1/fallas")
                    .hasAnyRole("ADMIN", "ANALISTA", "TECNICO")

                .requestMatchers(HttpMethod.GET, "/api/v1/fallas/cisterna/*")
                    .hasAnyRole("ADMIN", "ANALISTA", "TECNICO")

                .requestMatchers(HttpMethod.POST, "/api/v1/fallas")
                    .hasRole("TECNICO")

                .requestMatchers(HttpMethod.PATCH, "/api/v1/fallas/estado")
                    .hasRole("TECNICO")
                // =========================================================


                // =========================================================
                // HISTORIAL MANTENIMIENTO
                .requestMatchers(HttpMethod.GET, "/api/v1/historial")
                    .hasAnyRole("ADMIN", "ANALISTA", "TECNICO")

                .requestMatchers(HttpMethod.GET, "/api/v1/historial/cisterna/*")
                    .hasAnyRole("ADMIN", "ANALISTA", "TECNICO")

                .requestMatchers(HttpMethod.GET, "/api/v1/historial/nombre/*")
                    .hasAnyRole("ADMIN", "ANALISTA", "TECNICO")

                .requestMatchers(HttpMethod.GET, "/api/v1/historial/fecha/*")
                    .hasAnyRole("ADMIN", "ANALISTA", "TECNICO")

                .requestMatchers(HttpMethod.POST, "/api/v1/historial")
                    .hasRole("TECNICO")
                // =========================================================



                // =========================================================
                // ATENCION
                .requestMatchers(HttpMethod.GET, "/api/v1/atenciones/asignadas", "/api/v1/atenciones/asignadas/*")
                    .hasRole("CONDUCTOR")
                .requestMatchers(HttpMethod.GET, "/api/v1/atenciones/mis-atenciones")
                    .hasRole("CLIENTE")

                .requestMatchers(HttpMethod.GET, "/api/v1/atenciones")
                    .hasAnyRole("ADMIN", "ANALISTA", "PROGRAMADOR", "TECNICO")

                .requestMatchers(HttpMethod.GET, "/api/v1/atenciones/estado")
                    .hasAnyRole("ADMIN", "ANALISTA", "PROGRAMADOR")

                .requestMatchers(HttpMethod.GET, "/api/v1/atenciones/pedido/*")
                    .hasAnyRole("ADMIN", "ANALISTA", "PROGRAMADOR")

                .requestMatchers(HttpMethod.GET, "/api/v1/atenciones/*")
                    .hasAnyRole("ADMIN", "ANALISTA", "PROGRAMADOR")

                .requestMatchers(HttpMethod.POST, "/api/v1/atenciones")
                    .hasRole("PROGRAMADOR")

                .requestMatchers(HttpMethod.PATCH, "/api/v1/atenciones/*/iniciar")
                    .hasRole("CONDUCTOR")

                .requestMatchers(HttpMethod.PATCH, "/api/v1/atenciones/*/finalizar")
                    .hasRole("CONDUCTOR")

                .requestMatchers(HttpMethod.PATCH, "/api/v1/atenciones/*/cancelar")
                    .hasRole("PROGRAMADOR")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/atenciones/*")
                    .hasRole("PROGRAMADOR")
                // =========================================================



                // =========================================================
                // ALERTAS
                // CONDUCTOR: solo las alertas de SUS atenciones (antes veía las de todos los conductores)
                .requestMatchers(HttpMethod.GET, "/api/v1/alertas/mis-alertas")
                    .hasRole("CONDUCTOR")

                .requestMatchers(HttpMethod.GET, "/api/v1/alertas")
                    .hasAnyRole("ADMIN", "ANALISTA", "PROGRAMADOR")

                // CONDUCTOR permitido, pero el servicio verifica que la atención sea suya (404 si no)
                .requestMatchers(HttpMethod.GET, "/api/v1/alertas/atencion/*")
                    .hasAnyRole("ADMIN", "ANALISTA", "PROGRAMADOR", "CONDUCTOR")

                .requestMatchers(HttpMethod.GET, "/api/v1/alertas/estado")
                    .hasAnyRole("ADMIN", "ANALISTA", "PROGRAMADOR")

                .requestMatchers(HttpMethod.GET, "/api/v1/alertas/tipo")
                    .hasAnyRole("ADMIN", "ANALISTA", "PROGRAMADOR")

                .requestMatchers(HttpMethod.GET, "/api/v1/alertas/*")
                    .hasAnyRole("ADMIN", "ANALISTA", "PROGRAMADOR")

                .requestMatchers(HttpMethod.POST, "/api/v1/alertas")
                    .hasRole("CONDUCTOR")

                .requestMatchers(HttpMethod.PATCH, "/api/v1/alertas/*/revisar")
                    .hasRole("PROGRAMADOR")

                .requestMatchers(HttpMethod.PATCH, "/api/v1/alertas/*/atender")
                    .hasRole("PROGRAMADOR")
                // =========================================================

                .anyRequest()
                    .authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = new CookieCsrfTokenRepository();
        repository.setCookiePath("/auth");
        repository.setCookieCustomizer(cookie -> cookie.httpOnly(true).secure(cookieSecure).sameSite("Strict"));
        return repository;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(origin -> origin.trim()).toList());
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
