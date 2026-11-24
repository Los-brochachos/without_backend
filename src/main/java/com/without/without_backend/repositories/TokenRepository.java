package com.without.without_backend.repositories;
/*esto es una prueba de maria! */
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import com.without.without_backend.entities.usuario.Token;
import com.without.without_backend.entities.usuario.Usuario;

public interface TokenRepository extends JpaRepository<Token,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Token> findByToken(String token);

    Optional<List<Token>> findByUser(Usuario usuario);

}
