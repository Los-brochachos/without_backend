package com.without.without_backend.services.trabajador;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import com.without.without_backend.entities.trabajador.Trabajador;
import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.mappers.trabajador.TrabajadorMapper;
import com.without.without_backend.repositories.trabajador.TrabajadorRepository;
import com.without.without_backend.utils.RolesEnum;

class TrabajadorMeTest {
    private final TrabajadorRepository repository = mock(TrabajadorRepository.class);
    private final TrabajadorService service = new TrabajadorService(repository, new TrabajadorMapper(), null, null);
    @Test void obtieneNombreDelTrabajadorDelCorreoAutenticado() {
        var usuario = Usuario.builder().correo("personal@example.com").rol(RolesEnum.PROGRAMADOR).build();
        when(repository.findByUsuario_Correo(usuario.getCorreo())).thenReturn(Optional.of(Trabajador.builder().idTrabajador(7).nombres("Rodrigo").apellidos("Corilla").usuario(usuario).build()));
        var resultado = service.me(usuario.getCorreo());
        assertEquals("Rodrigo", resultado.nombres()); assertEquals("Corilla", resultado.apellidos());
        verify(repository).findByUsuario_Correo(usuario.getCorreo()); verify(repository, never()).findAll();
    }
    @Test void cuentaSinTrabajadorDevuelveNoEncontrado() {
        assertThrows(java.util.NoSuchElementException.class, () -> service.me("sin-perfil@example.com"));
        verify(repository, never()).findAll();
    }
}
