package com.without.without_backend.repositories.cliente;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.without.without_backend.entities.cliente.Cliente;
import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.utils.Estado;

public interface ClienteRepository extends JpaRepository<Cliente,Integer>{
    

    List<Cliente> findByUsuarioEstado(Estado estado);

    Optional<Cliente> findByUsuario(Usuario usuario);

    Optional<Cliente> findByIdClienteAndUsuarioEstado(
            Integer idCliente,
            Estado estado
    );

    // El RUC identifica a la empresa: no puede repetirse entre clientes.
    boolean existsByRuc(String ruc);

    boolean existsByRucAndIdClienteNot(String ruc, Integer idCliente);
} 