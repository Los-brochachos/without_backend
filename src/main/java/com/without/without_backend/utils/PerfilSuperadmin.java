package com.without.without_backend.utils;

import com.without.without_backend.entities.usuario.Usuario;

/**
 * Perfiles ocultos del SUPERADMIN.
 * La cuenta superadmin tiene sus propios perfiles de trabajador (conductor, programador y técnico) y de cliente
 * para poder usar todas las funciones; esos perfiles:
 * - no se muestran en los listados (trabajadores, conductores, programadores, técnicos, clientes);
 * - no pueden ser editados ni dados de baja desde la administración.
 */
public final class PerfilSuperadmin {
    private PerfilSuperadmin() {}

    public static boolean esSuperadmin(Usuario usuario) {
        return usuario != null && usuario.getRol() == RolesEnum.SUPERADMIN;
    }

    public static void proteger(Usuario usuario) {
        if (esSuperadmin(usuario)) {
            throw new IllegalArgumentException("Los perfiles del superadministrador no se pueden modificar ni dar de baja");
        }
    }
}
