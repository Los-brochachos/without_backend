package com.without.without_backend.utils;

public enum RolesEnum {
    ADMIN,
    PROGRAMADOR,
    CONDUCTOR,
    TECNICO,
    CLIENTE,
    ANALISTA,
    /** Cuenta única con acceso total (jerarquía de roles en SecurityConfig). No se crea por la API. */
    SUPERADMIN
}
