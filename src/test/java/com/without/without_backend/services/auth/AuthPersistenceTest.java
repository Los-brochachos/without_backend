package com.without.without_backend.services.auth;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import com.without.without_backend.support.TestDatabaseInitializer;
import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.repositories.usuario.UsuarioRepository;
import com.without.without_backend.services.AuthService;
import com.without.without_backend.services.JwtService;
import com.without.without_backend.utils.Estado;
import com.without.without_backend.utils.RolesEnum;
import com.without.without_backend.utils.auth.AuthRequest;

/** Usa exclusivamente withoutdb_test; las tablas son temporales y los datos se revierten. */
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestDatabaseInitializer.class)
@Transactional
class AuthPersistenceTest {
    @Autowired private UsuarioRepository users;
    @Autowired private PasswordEncoder encoder;
    @Autowired private AuthService auth;
    @Autowired private JwtService jwt;

    @Test void loginRotacionYLogoutFuncionanConJpaReal() {
        Usuario user = users.saveAndFlush(Usuario.builder().correo("test-" + UUID.randomUUID() + "@without.example")
            .contraseña(encoder.encode("Prueba1234*")).estado(Estado.ACTIVO).rol(RolesEnum.CLIENTE).build());
        var initial = auth.authenticate(new AuthRequest(user.getCorreo(), "Prueba1234*"));
        assertTrue(jwt.isTokenValid(initial.accessToken(), user));
        var rotated = auth.refreshToken(initial.refreshToken());
        assertNotEquals(initial.refreshToken(), rotated.refreshToken());
        assertTrue(jwt.isTokenValid(rotated.accessToken(), user));
        auth.logout(rotated.refreshToken());
        assertThrows(BadCredentialsException.class, () -> auth.refreshToken(initial.refreshToken()));
        assertThrows(BadCredentialsException.class, () -> auth.refreshToken(rotated.refreshToken()));
    }
}
