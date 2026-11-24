package com.without.without_backend.repositories.usuario;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.utils.RolesEnum;

public interface UsuarioRepository extends JpaRepository<Usuario,Integer>{
    Optional<Usuario> findByCorreo(String correo);

    /** Para garantizar que exista un solo SUPERADMIN. */
    boolean existsByRol(RolesEnum rol);
    
}
