package com.without.without_backend.services.usuario;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.repositories.TokenRepository;
import com.without.without_backend.repositories.trabajador.TrabajadorRepository;
import com.without.without_backend.repositories.usuario.UsuarioRepository;
import com.without.without_backend.services.trabajador.TrabajadorService;
import com.without.without_backend.utils.Estado;
import com.without.without_backend.utils.PerfilSuperadmin;

import lombok.RequiredArgsConstructor;

/**
 * Activar / desactivar cuentas desde el listado de Usuarios (ADMIN).
 * - Cuenta de un trabajador: usa las mismas reglas que Trabajadores (TrabajadorService.cambiarActivo):
 *   no desactivarse a sí mismo, no desactivar un conductor con atenciones activas, y cambian juntos
 *   el estado del trabajador y el de su cuenta.
 * - Cuenta de un cliente: cambia el estado de la cuenta (igual que DELETE /clientes/{id}).
 * - La cuenta SUPERADMIN no se puede desactivar.
 * Al desactivar se cierran sus sesiones (refresh tokens). Separado de UsuarioService, que participa en el filtro JWT.
 */
@Service
@RequiredArgsConstructor
public class EstadoCuentaService {
    private final UsuarioRepository usuarios;
    private final TrabajadorRepository trabajadores;
    private final TrabajadorService trabajadorService;
    private final TokenRepository tokens;

    @Transactional
    public void cambiarEstado(Integer idUsuario, boolean activo, String correoActual) {
        Usuario usuario = usuarios.findById(idUsuario).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        PerfilSuperadmin.proteger(usuario);
        if (!activo && usuario.getCorreo().equalsIgnoreCase(correoActual)) {
            throw new IllegalArgumentException("No puedes desactivar tu propia cuenta");
        }
        var trabajador = trabajadores.findByUsuario(usuario);
        if (trabajador.isPresent()) {
            trabajadorService.cambiarActivo(trabajador.get().getIdTrabajador(), activo, correoActual);
        } else {
            usuario.setEstado(activo ? Estado.ACTIVO : Estado.INACTIVO);
            usuarios.save(usuario);
        }
        if (!activo) tokens.findByUser(usuario).ifPresent(tokens::deleteAll);
    }
}
