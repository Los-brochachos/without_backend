package com.without.without_backend.support;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.env.MockEnvironment;

/** Prueba solo la barrera de configuracion; nunca crea conexiones. */
class TestDatabaseInitializerTest {
    private MockEnvironment validEnvironment() {
        MockEnvironment env = new MockEnvironment()
            .withProperty("spring.datasource.url", "jdbc:mysql://localhost:3306/withoutdb_test")
            .withProperty("spring.datasource.username", "usuario_solo_test")
            .withProperty("spring.datasource.password", "valor-ficticio-no-es-una-credencial")
            .withProperty("spring.jpa.hibernate.ddl-auto", "create-drop");
        env.setActiveProfiles("test");
        return env;
    }

    @Test void permiteSoloDestinoDePruebas() {
        assertDoesNotThrow(() -> TestDatabaseInitializer.validate(validEnvironment()));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "jdbc:mysql://localhost:3306/withoutdb",
        "jdbc:mysql://localhost:3306/withoutdb_test_extra",
        "jdbc:mysql://servidor-remoto:3306/withoutdb_test",
        "jdbc:mysql://localhost:3307/withoutdb_test",
        "jdbc:mysql://localhost:3306/withoutdb_test?createDatabaseIfNotExist=true",
        "jdbc:mysql://localhost:3306/withoutdb_test#otro",
        "jdbc:mysql://usuario@localhost:3306/withoutdb_test",
        "jdbc:mysql://localhost:3306/%77ithoutdb_test",
        "jdbc:h2:mem:withoutdb_test"
    })
    void rechazaCambiosDeDestinoAntesDeAbrirConexion(String url) {
        var env = validEnvironment().withProperty("spring.datasource.url", url);
        assertThrows(IllegalStateException.class, () -> TestDatabaseInitializer.validate(env));
    }

    @ParameterizedTest
    @ValueSource(strings = {"dev", "prod"})
    void rechazaSeederYEntornoProductivo(String profile) {
        var env = validEnvironment();
        env.setActiveProfiles("test", profile);
        assertThrows(IllegalStateException.class, () -> TestDatabaseInitializer.validate(env));
    }

    @Test void rechazaAusenciaDePerfilTest() {
        var env = validEnvironment();
        env.setActiveProfiles("dev");
        assertThrows(IllegalStateException.class, () -> TestDatabaseInitializer.validate(env));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "${TEST_DB_USER}"})
    void rechazaUsuarioFaltante(String user) {
        var env = validEnvironment().withProperty("spring.datasource.username", user);
        assertThrows(IllegalStateException.class, () -> TestDatabaseInitializer.validate(env));
    }

    @Test void permiteUsuarioLocalAutorizadoSoloEnBaseTest() {
        var env = validEnvironment().withProperty("spring.datasource.username", "root");
        assertDoesNotThrow(() -> TestDatabaseInitializer.validate(env));
        env.withProperty("spring.datasource.url", "jdbc:mysql://localhost:3306/withoutdb");
        assertThrows(IllegalStateException.class, () -> TestDatabaseInitializer.validate(env));
    }

    @Test void rechazaPasswordAusente() {
        var env = validEnvironment().withProperty("spring.datasource.password", "");
        assertThrows(IllegalStateException.class, () -> TestDatabaseInitializer.validate(env));
    }

    @Test void rechazaPoliticaDeEsquemaDistinta() {
        var env = validEnvironment().withProperty("spring.jpa.hibernate.ddl-auto", "update");
        assertThrows(IllegalStateException.class, () -> TestDatabaseInitializer.validate(env));
    }
}
