package com.without.without_backend.services.usuario;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.without.without_backend.dto.usuario.CambiarClaveRequest;
import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.repositories.usuario.UsuarioRepository;

class SeguridadCuentaTest {
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final SeguridadCuentaService service = new SeguridadCuentaService(usuarios, encoder);
    private final Usuario usuario = Usuario.builder().correo("cliente@example.com").contraseña("hash-anterior").build();
    private void preparar() { when(usuarios.findByCorreo(usuario.getCorreo())).thenReturn(Optional.of(usuario)); }
    @Test void validaClaveActualAntesDeGuardarNuevaClaveEncriptada() {
        preparar(); when(encoder.matches("actual", "hash-anterior")).thenReturn(true);
        when(encoder.encode("Nueva1234*")).thenReturn("hash-nuevo");
        service.cambiarClave(usuario.getCorreo(), new CambiarClaveRequest("actual", "Nueva1234*"));
        assertEquals("hash-nuevo", usuario.getContraseña()); verify(usuarios).save(usuario);
    }
    @Test void claveActualIncorrectaNoModificaLaCuenta() {
        preparar(); assertThrows(IllegalArgumentException.class, () -> service.cambiarClave(usuario.getCorreo(), new CambiarClaveRequest("incorrecta", "Nueva1234*")));
        verify(usuarios, never()).save(any()); verify(encoder, never()).encode(any());
    }
    @Test void noPermiteReutilizarLaMismaClave() {
        preparar(); when(encoder.matches("Igual1234*", "hash-anterior")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.cambiarClave(usuario.getCorreo(), new CambiarClaveRequest("Igual1234*", "Igual1234*")));
        verify(usuarios, never()).save(any());
    }
    @Test void validaLimiteDeBytesDelCifrado() {
        preparar(); when(encoder.matches("actual", "hash-anterior")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.cambiarClave(usuario.getCorreo(), new CambiarClaveRequest("actual", "á".repeat(40))));
        verify(usuarios, never()).save(any());
    }
}
