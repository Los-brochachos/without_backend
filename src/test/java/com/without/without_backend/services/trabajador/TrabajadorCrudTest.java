package com.without.without_backend.services.trabajador;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.without.without_backend.dto.trabajador.*;
import com.without.without_backend.entities.trabajador.Trabajador;
import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.mappers.trabajador.TrabajadorMapper;
import com.without.without_backend.repositories.trabajador.TrabajadorRepository;
import com.without.without_backend.repositories.usuario.UsuarioRepository;
import com.without.without_backend.utils.*;

class TrabajadorCrudTest {
    final TrabajadorRepository trabajadores = mock(TrabajadorRepository.class);
    final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    final PasswordEncoder encoder = mock(PasswordEncoder.class);
    final TrabajadorService service = new TrabajadorService(trabajadores, new TrabajadorMapper(), usuarios, encoder);
    final LocalDate fecha = LocalDate.of(2026, 1, 1);
    TrabajadorCreateRequest solicitud() { return new TrabajadorCreateRequest("Ana", "Prueba", "12345678", "987654321", "Lima", fecha, RolesEnum.ANALISTA, "ana@example.com", "Prueba1234"); }
    Trabajador preparar(RolesEnum rol) {
        var t = Trabajador.builder().idTrabajador(7).nombres("Ana").apellidos("Prueba").dni("12345678").estado(Estado.ACTIVO)
            .usuario(Usuario.builder().correo("ana@example.com").rol(rol).estado(Estado.ACTIVO).build()).build();
        when(trabajadores.findById(7)).thenReturn(Optional.of(t));
        when(trabajadores.save(any())).thenAnswer(i -> i.getArgument(0)); return t;
    }
    @Test void registraDatosNuevosConClaveCifrada() {
        when(encoder.encode("Prueba1234")).thenReturn("hash");
        when(trabajadores.save(any())).thenAnswer(i -> { Trabajador t = i.getArgument(0); t.setIdTrabajador(7); return t; });
        var r = service.crear(solicitud());
        assertEquals(7, r.idTrabajador()); assertEquals(RolesEnum.ANALISTA, r.rol());
        verify(usuarios).save(argThat(u -> "hash".equals(u.getPassword()) && u.getEstado() == Estado.ACTIVO));
    }
    @Test void rechazaCorreoDuplicadoSinCrearTrabajador() {
        when(usuarios.findByCorreo("ana@example.com")).thenReturn(Optional.of(new Usuario()));
        assertThrows(IllegalArgumentException.class, () -> service.crear(solicitud()));
        verify(trabajadores, never()).save(any());
    }
    @Test void rechazaDniDuplicadoSinCrearCuenta() {
        when(trabajadores.findByDni("12345678")).thenReturn(Optional.of(new Trabajador()));
        assertThrows(IllegalArgumentException.class, () -> service.crear(solicitud()));
        verify(usuarios, never()).save(any());
    }
    @Test void editaDatosComunesSinCambiarRolNiCuenta() {
        var t = preparar(RolesEnum.PROGRAMADOR); var usuario = t.getUsuario();
        var r = service.actualizar(7, new TrabajadorUpdateRequest("Ana María", "Prueba", "12345678", "987654321", "Nueva dirección", fecha));
        assertEquals("Ana María", r.nombres()); assertEquals(RolesEnum.PROGRAMADOR, r.rol());
        assertSame(usuario, t.getUsuario()); assertEquals("ana@example.com", r.correo());
    }
    @Test void rechazaDniDeOtroTrabajador() {
        var t = preparar(RolesEnum.ANALISTA);
        when(trabajadores.findByDni("87654321")).thenReturn(Optional.of(Trabajador.builder().idTrabajador(8).build()));
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(7,
            new TrabajadorUpdateRequest("Otro", "Apellido", "87654321", "", "", fecha)));
        assertEquals("Ana", t.getNombres()); verify(trabajadores, never()).save(any());
    }
    @Test void bajaLogicaDeshabilitaCuentaYConservaRegistro() {
        var t = preparar(RolesEnum.ANALISTA);
        assertEquals(Estado.INACTIVO, service.cambiarActivo(7, false, "admin@example.com").estado());
        assertEquals(Estado.INACTIVO, t.getUsuario().getEstado()); verify(trabajadores, never()).delete(any());
    }
    @Test void reactivaTrabajadorYCuenta() {
        var t = preparar(RolesEnum.ANALISTA); t.setEstado(Estado.INACTIVO); t.getUsuario().setEstado(Estado.INACTIVO);
        service.cambiarActivo(7, true, "admin@example.com");
        assertEquals(Estado.ACTIVO, t.getEstado()); assertTrue(t.getUsuario().isEnabled());
    }
    @Test void impideDesactivarCuentaPropia() {
        var t = preparar(RolesEnum.ADMIN);
        assertThrows(IllegalArgumentException.class, () -> service.cambiarActivo(7, false, "ana@example.com"));
        assertEquals(Estado.ACTIVO, t.getEstado()); verify(usuarios, never()).save(any());
    }
    @Test void noDesactivaConductorConVisitasActivas() {
        var t = preparar(RolesEnum.CONDUCTOR); when(trabajadores.contarAtencionesActivas(eq(7), any())).thenReturn(1L);
        assertThrows(IllegalArgumentException.class, () -> service.cambiarActivo(7, false, "admin@example.com"));
        assertEquals(Estado.ACTIVO, t.getEstado()); verify(usuarios, never()).save(any());
    }
}
