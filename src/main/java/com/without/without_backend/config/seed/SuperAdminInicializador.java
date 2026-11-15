package com.without.without_backend.config.seed;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.without.without_backend.entities.cliente.Cliente;
import com.without.without_backend.entities.trabajador.Conductor;
import com.without.without_backend.entities.trabajador.Programador;
import com.without.without_backend.entities.trabajador.Tecnico;
import com.without.without_backend.entities.trabajador.Trabajador;
import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.utils.trabajador.CategoriaLicencia;
import com.without.without_backend.utils.trabajador.EspecialidadTecnico;
import com.without.without_backend.utils.trabajador.NivelIngles;
import com.without.without_backend.utils.trabajador.NivelTecnico;
import com.without.without_backend.utils.trabajador.Turno;
import com.without.without_backend.repositories.usuario.UsuarioRepository;
import com.without.without_backend.utils.Estado;
import com.without.without_backend.utils.RolesEnum;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Garantiza que exista UNA sola cuenta SUPERADMIN, aunque la base ya tenga datos
 * (el DatabaseSeeder solo carga datos en una base vacía).
 * - Se ejecuta después del seeder (@Order(2)).
 * - Si la columna usuario.rol es un ENUM de MySQL creado antes de existir SUPERADMIN, la amplía
 *   (ddl-auto=update no modifica columnas existentes).
 * - No hay endpoint para crear SUPERADMIN: /trabajadores solo admite ADMIN y ANALISTA.
 */
@Component
@Profile("dev")
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class SuperAdminInicializador implements CommandLineRunner {
    /** Cuenta: superadmin@without.example (misma contraseña de desarrollo que el seed). */
    public static final String CUENTA = "superadmin";
    private static final String CONTRASENA_DESARROLLO = "without1234*";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        ampliarColumnaRol();
        Usuario superadmin = entityManager.createQuery("select u from Usuario u where u.rol = :rol", Usuario.class)
            .setParameter("rol", RolesEnum.SUPERADMIN).getResultStream().findFirst().orElse(null);
        if (superadmin == null) {
            superadmin = usuarios.save(Usuario.builder()
                .correo(CUENTA + "@without.example")
                .contraseña(passwordEncoder.encode(CONTRASENA_DESARROLLO))
                .estado(Estado.ACTIVO)
                .rol(RolesEnum.SUPERADMIN)
                .build());
            log.info("Cuenta SUPERADMIN creada: {}@without.example", CUENTA);
        }
        crearPerfiles(superadmin);
    }

    /**
     * Perfiles propios (ocultos, ver PerfilSuperadmin) para usar todas las funciones:
     * un trabajador que es a la vez conductor, programador y técnico, y un cliente. Solo crea los que falten.
     */
    private void crearPerfiles(Usuario superadmin) {
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Lima"));
        Trabajador trabajador = entityManager.createQuery("select t from Trabajador t where t.usuario = :u", Trabajador.class)
            .setParameter("u", superadmin).getResultStream().findFirst().orElse(null);
        if (trabajador == null) {
            trabajador = Trabajador.builder().usuario(superadmin).nombres("Super").apellidos("Administrador")
                .dni("99999999").telefono("999999999").direccion("Oficina central WITHOUT")
                .fechaIngreso(hoy).estado(Estado.ACTIVO).build();
            entityManager.persist(trabajador);
        }
        if (entityManager.find(Conductor.class, trabajador.getIdTrabajador()) == null)
            entityManager.persist(Conductor.builder().trabajador(trabajador).licenciaConducir("Q99999999")
                .categoriaLicencia(CategoriaLicencia.A_IIIC).fechaVencimientoLicencia(hoy.plusYears(10)).build());
        if (entityManager.find(Programador.class, trabajador.getIdTrabajador()) == null)
            entityManager.persist(Programador.builder().trabajador(trabajador).nivelIngles(NivelIngles.C2).turno(Turno.MAÑANA).build());
        if (entityManager.find(Tecnico.class, trabajador.getIdTrabajador()) == null)
            entityManager.persist(Tecnico.builder().trabajador(trabajador).especialidad(EspecialidadTecnico.MANTENIMIENTO_INDUSTRIAL)
                .nivelTecnico(NivelTecnico.SENIOR).build());
        boolean tieneCliente = !entityManager.createQuery("select c from Cliente c where c.usuario = :u", Cliente.class)
            .setParameter("u", superadmin).setMaxResults(1).getResultList().isEmpty();
        if (!tieneCliente)
            entityManager.persist(Cliente.builder().ruc("20999999999").razonSocial("Superadministración WITHOUT")
                .direccion("Oficina central WITHOUT").telefono("999999999").usuario(superadmin).build());
    }

    /** Agrega SUPERADMIN al ENUM de MySQL si falta. En otras bases de datos (o columnas VARCHAR) no hace nada. */
    private void ampliarColumnaRol() {
        try {
            List<?> tipo = entityManager.createNativeQuery(
                "SELECT COLUMN_TYPE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'usuario' AND COLUMN_NAME = 'rol'")
                .getResultList();
            if (tipo.isEmpty() || !String.valueOf(tipo.get(0)).startsWith("enum(") || String.valueOf(tipo.get(0)).contains("'SUPERADMIN'")) return;
            entityManager.createNativeQuery(
                "ALTER TABLE usuario MODIFY rol ENUM('ADMIN','ANALISTA','CLIENTE','CONDUCTOR','PROGRAMADOR','TECNICO','SUPERADMIN') NOT NULL")
                .executeUpdate();
            log.info("Columna usuario.rol ampliada con el valor SUPERADMIN");
        } catch (RuntimeException e) {
            log.warn("No se pudo revisar la columna usuario.rol: {}", e.getMessage());
        }
    }
}
