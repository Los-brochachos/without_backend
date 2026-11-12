package com.without.without_backend.services.cliente;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
import com.without.without_backend.dto.cliente.ClienteCreateRequest;
import com.without.without_backend.dto.cliente.ClienteUpdateRequest;
import com.without.without_backend.handler.GlobalExceptionHandler;
import com.without.without_backend.entities.cliente.Cliente;
import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.mappers.cliente.ClienteMapper;
import com.without.without_backend.repositories.cliente.ClienteRepository;
import com.without.without_backend.repositories.usuario.UsuarioRepository;
import com.without.without_backend.utils.RolesEnum;
import com.without.without_backend.utils.Estado;

class RegistroClienteTest {
    private final ClienteRepository clientes = mock(ClienteRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final ClienteService service = new ClienteService(clientes, usuarios, new ClienteMapper(), encoder);
    private final ClienteCreateRequest request = new ClienteCreateRequest("20123456789", "Empresa de prueba", "Dirección", "999999999", "cliente@ejemplo.com", "Prueba1234*");
    @Test void registroPublicoCreaSoloClienteYEncriptaClave() {
        when(encoder.encode(request.contraseña())).thenReturn("clave-encriptada");
        when(usuarios.save(any(Usuario.class))).thenAnswer(i -> {
            Usuario u = i.getArgument(0);
            assertEquals(RolesEnum.CLIENTE, u.getRol()); assertEquals(Estado.ACTIVO, u.getEstado());
            assertEquals("clave-encriptada", u.getContraseña()); u.setIdUsuario(7); return u;
        });
        when(clientes.save(any(Cliente.class))).thenAnswer(i -> { Cliente c = i.getArgument(0); c.setIdCliente(9); return c; });
        var resultado = service.crearCliente(request);
        assertEquals(9, resultado.cliente().idCliente());
        assertEquals(RolesEnum.CLIENTE, resultado.cliente().rol());
        verify(encoder).encode(request.contraseña());
    }
    @Test void correoDuplicadoSeRechazaSinGuardar() {
        when(usuarios.findByCorreo(request.correo())).thenReturn(Optional.of(Usuario.builder().build()));
        assertEquals("El correo ya está registrado", assertThrows(IllegalArgumentException.class, () -> service.crearCliente(request)).getMessage());
        verify(usuarios, never()).save(any()); verify(clientes, never()).save(any());
    }

    // ===== RUC único =====
    private final ClienteUpdateRequest cambioRuc = new ClienteUpdateRequest("20999999999", null, null, null, null, null);

    private Cliente clienteExistente() {
        Usuario usuario = Usuario.builder().idUsuario(7).correo("cliente@ejemplo.com").estado(Estado.ACTIVO).rol(RolesEnum.CLIENTE).build();
        return Cliente.builder().idCliente(9).ruc("20123456789").razonSocial("Empresa de prueba").usuario(usuario).build();
    }

    @Test void rucDuplicadoEnRegistroSeRechazaSinCrearUsuario() {
        when(clientes.existsByRuc(request.ruc())).thenReturn(true);
        var error = assertThrows(IllegalArgumentException.class, () -> service.crearCliente(request));
        assertEquals("El RUC ya está registrado por otra empresa", error.getMessage());
        // No debe quedar un usuario huérfano sin cliente.
        verify(usuarios, never()).save(any()); verify(clientes, never()).save(any());
    }

    @Test void adminNoPuedeAsignarRucDeOtraEmpresa() {
        Cliente cliente = clienteExistente();
        when(clientes.findById(9)).thenReturn(Optional.of(cliente));
        when(clientes.existsByRucAndIdClienteNot("20999999999", 9)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.actualizarCliente(9, cambioRuc));
        assertEquals("20123456789", cliente.getRuc());
        verify(clientes, never()).save(any());
    }

    @Test void clienteNoPuedeCambiarseAUnRucAjenoDesdeSuPerfil() {
        Cliente cliente = clienteExistente();
        when(usuarios.findByCorreo("cliente@ejemplo.com")).thenReturn(Optional.of(cliente.getUsuario()));
        when(clientes.findByUsuario(cliente.getUsuario())).thenReturn(Optional.of(cliente));
        when(clientes.existsByRucAndIdClienteNot("20999999999", 9)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.actualizarMe("cliente@ejemplo.com", cambioRuc));
        assertEquals("20123456789", cliente.getRuc());
    }

    @Test void rucNuevoLibreSeActualiza() {
        Cliente cliente = clienteExistente();
        when(clientes.findById(9)).thenReturn(Optional.of(cliente));
        when(clientes.existsByRucAndIdClienteNot("20999999999", 9)).thenReturn(false);
        service.actualizarCliente(9, cambioRuc);
        assertEquals("20999999999", cliente.getRuc());
    }

    @Test void reenviarElMismoRucNoSeConsideraDuplicado() {
        Cliente cliente = clienteExistente();
        when(clientes.findById(9)).thenReturn(Optional.of(cliente));
        service.actualizarCliente(9, new ClienteUpdateRequest("20123456789", "Nuevo nombre", null, null, null, null));
        assertEquals("Nuevo nombre", cliente.getRazonSocial());
        verify(clientes, never()).existsByRucAndIdClienteNot(any(), any());
    }

    @Test void restriccionUnicaDeBdRespondeConflictoSinExponerSql() {
        var respuesta = new GlobalExceptionHandler().handleDataIntegrityViolation(
            new DataIntegrityViolationException("Duplicate entry '20123456789' for key 'cliente.UK_ruc'"));
        assertEquals(409, respuesta.getStatusCode().value());
        assertFalse(String.valueOf(respuesta.getBody()).contains("Duplicate entry"));
    }
}
