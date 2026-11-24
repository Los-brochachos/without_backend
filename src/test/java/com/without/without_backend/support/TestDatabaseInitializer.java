package com.without.without_backend.support;

import java.net.URI;
import java.util.Arrays;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

/** Valida el destino ANTES de crear DataSource, EntityManager o tablas. */
public class TestDatabaseInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext context) {
        validate(context.getEnvironment());
    }

    public static void validate(Environment env) {
        var profiles = Arrays.asList(env.getActiveProfiles());
        if (!profiles.contains("test") || profiles.contains("dev") || profiles.contains("prod")) {
            throw new IllegalStateException("Las pruebas con MySQL requieren solo el entorno test, sin dev/prod");
        }
        String url = env.getRequiredProperty("spring.datasource.url");
        if (!url.startsWith("jdbc:mysql://")) {
            throw new IllegalStateException("El destino de pruebas debe ser MySQL sin conexiones alternativas");
        }
        URI uri = URI.create(url.substring("jdbc:".length()));
        if (!"localhost".equals(uri.getHost()) || uri.getPort() != 3306
                || !"/withoutdb_test".equals(uri.getRawPath()) || uri.getRawUserInfo() != null
                || uri.getRawQuery() != null || uri.getRawFragment() != null) {
            throw new IllegalStateException("Destino rechazado: se requiere withoutdb_test sin parametros JDBC");
        }
        String user = credential(env, "spring.datasource.username");
        String password = credential(env, "spring.datasource.password");
        if (user.isBlank() || user.contains("${")
                || password.isBlank() || password.contains("${")) {
            throw new IllegalStateException("Faltan credenciales locales de MySQL para las pruebas");
        }
        if (!"create-drop".equals(env.getProperty("spring.jpa.hibernate.ddl-auto"))) {
            throw new IllegalStateException("El ciclo de tablas de pruebas debe ser create-drop");
        }
    }

    private static String credential(Environment env, String key) {
        try {
            return env.getRequiredProperty(key);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Faltan credenciales locales de MySQL para las pruebas");
        }
    }
}
