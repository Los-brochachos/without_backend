package com.without.without_backend.config;

import java.util.List;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Documentación OpenAPI 3 de la API WITHOUT.
 * - Swagger UI:  http://localhost:8080/swagger-ui.html
 * - JSON:        http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    public static final String BEARER = "bearerAuth";

    private static final String DESCRIPCION = """
        API REST del sistema **WITHOUT** para la gestión de pedidos de gases industriales,
        atenciones (entregas), cisternas, fallas, mantenimiento, clientes y trabajadores.

        ### Cómo autenticarse en esta página
        1. **GET `/auth/csrf`** → copia el valor de `token`.
        2. **POST `/auth/login`** → pega ese valor en el header `X-XSRF-TOKEN` y envía
           `{"correo": "...", "contraseña": "..."}`. La respuesta trae `access_token`
           (JWT, 15 min) y el navegador guarda el refresh token en una cookie `HttpOnly`.
        3. Pulsa **Authorize** (candado) y pega solo el `access_token` (sin escribir "Bearer").
        4. Ya puedes probar los endpoints protegidos según tu rol.

        ### Roles
        `ADMIN`, `ANALISTA`, `PROGRAMADOR`, `CONDUCTOR`, `TECNICO`, `CLIENTE`.
        Cada operación indica en su descripción qué roles pueden usarla.

        ### Códigos de error comunes
        | Código | Significado |
        |---|---|
        | 400 | Datos inválidos o regla de negocio incumplida |
        | 401 | Sin token, token vencido o inválido |
        | 403 | El rol no tiene permiso para esta operación |
        | 404 | Recurso inexistente o no pertenece al usuario |
        """;

    @Bean
    OpenAPI withoutOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("WITHOUT API")
                .version("v1")
                .description(DESCRIPCION))
            .servers(List.of(new Server().url("http://localhost:8080").description("Desarrollo local")))
            // Todas las operaciones requieren JWT salvo las marcadas como públicas.
            .addSecurityItem(new SecurityRequirement().addList(BEARER))
            .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                .name(BEARER)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Access token obtenido en POST /auth/login (campo access_token).")));
    }

    /** Grupo por defecto: excluye el controlador de prueba. */
    @Bean
    GroupedOpenApi apiWithout() {
        return GroupedOpenApi.builder()
            .group("without-api")
            .displayName("WITHOUT API v1")
            .pathsToMatch("/api/v1/**", "/auth/**")
            .build();
    }
}
