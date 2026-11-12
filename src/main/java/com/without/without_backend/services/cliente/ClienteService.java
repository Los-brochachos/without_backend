package com.without.without_backend.services.cliente;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.without.without_backend.dto.cliente.ClienteCreateRequest;
import com.without.without_backend.dto.cliente.ClienteResponse;
import com.without.without_backend.dto.cliente.ClienteUpdateRequest;
import com.without.without_backend.entities.cliente.Cliente;
import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.mappers.cliente.ClienteMapper;
import com.without.without_backend.repositories.cliente.ClienteRepository;
import com.without.without_backend.repositories.usuario.UsuarioRepository;
import com.without.without_backend.utils.Estado;
import com.without.without_backend.utils.RolesEnum;
import com.without.without_backend.utils.auth.RegisterResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteMapper clienteMapper;
    private final PasswordEncoder passwordEncoder;

    static final String RUC_DUPLICADO = "El RUC ya está registrado por otra empresa";

    /** Cambia el RUC solo si es distinto y no pertenece a otro cliente. */
    private void actualizarRuc(Cliente cliente, String nuevoRuc) {
        if (nuevoRuc == null || nuevoRuc.equals(cliente.getRuc())) {
            return;
        }
        if (clienteRepository.existsByRucAndIdClienteNot(nuevoRuc, cliente.getIdCliente())) {
            throw new IllegalArgumentException(RUC_DUPLICADO);
        }
        cliente.setRuc(nuevoRuc);
    }

    public List<ClienteResponse> listarClientesTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(clienteMapper::toDto)
                .toList();
    }

    public List<ClienteResponse> listarActivos() {
        return clienteRepository.findByUsuarioEstado(Estado.ACTIVO)
                .stream()
                .map(clienteMapper::toDto)
                .toList();
    }

    public ClienteResponse buscarPorId(Integer id) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Cliente no encontrado"));

        return clienteMapper.toDto(cliente);
    }

    public ClienteResponse buscarActivoPorId(Integer id) {

        Cliente cliente = clienteRepository
                .findByIdClienteAndUsuarioEstado(
                        id,
                        Estado.ACTIVO
                ).orElseThrow(() ->new NoSuchElementException("Cliente no encontrado"));

        return clienteMapper.toDto(cliente);
    }

    @Transactional
    public RegisterResponse crearCliente(ClienteCreateRequest request) {

        // 1. Verificar que el correo no esté registrado
        if (usuarioRepository.findByCorreo(request.correo()).isPresent()) {
            throw new IllegalArgumentException(
                    "El correo ya está registrado");
        }

        // 1b. Verificar que el RUC no pertenezca a otra empresa cliente
        if (clienteRepository.existsByRuc(request.ruc())) {
            throw new IllegalArgumentException(RUC_DUPLICADO);
        }

        // 2. Crear Usuario
        Usuario usuario = Usuario.builder()
                .correo(request.correo())
                .contraseña(
                        passwordEncoder.encode(request.contraseña())
                )
                .estado(Estado.ACTIVO)
                .rol(RolesEnum.CLIENTE)
                .build();

        usuario = usuarioRepository.save(usuario);

        // 3. Crear Cliente asociado al Usuario
        Cliente cliente = clienteMapper.toEntity(
                request,
                usuario
        );

        cliente = clienteRepository.save(cliente);


        // 4. Devolver respuesta
        return new RegisterResponse(clienteMapper.toDto(cliente), "INFORMACIÓN", "Nuevo usuario registrado.");
    }

    @Transactional
    public ClienteResponse actualizarCliente(
            Integer id,
            ClienteUpdateRequest request) {

        // 1. Buscar Cliente
        Cliente cliente = clienteRepository.findById(id)
                        .orElseThrow(() -> new NoSuchElementException("Cliente no encontrado"));
        
        Usuario usuario = cliente.getUsuario();

        // 2. Actualizar RUC (sin chocar con el de otra empresa)
        actualizarRuc(cliente, request.ruc());

        // 3. Actualizar Razón Social
        if (request.razonSocial() != null) {
            cliente.setRazonSocial(request.razonSocial());
        }

        // 4. Actualizar Dirección
        if (request.direccion() != null) {
            cliente.setDireccion(request.direccion());
        }

        // 5. Actualizar Teléfono
        if (request.telefono() != null) {
            cliente.setTelefono(request.telefono());
        }

        // 6. Actualizar Correo
        if (request.correo() != null
                && !usuario.getCorreo().equals(request.correo())) {

            if (usuarioRepository
                    .findByCorreo(request.correo())
                    .isPresent()) {

                throw new IllegalArgumentException(
                        "El correo ya está registrado");
            }

            usuario.setCorreo(request.correo());
        }

        // 7. Actualizar contraseña solo si fue enviada
        if (request.contraseña() != null
                && !request.contraseña().isBlank()) {

            usuario.setContraseña(
                    passwordEncoder.encode(
                            request.contraseña()
                    )
            );
        }

        // 8. Guardar cambios
        usuarioRepository.save(usuario);
        clienteRepository.save(cliente);

        // 9. Devolver respuesta
        return clienteMapper.toDto(cliente);
    }

    @Transactional
    public void eliminarCliente(Integer id) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Cliente no encontrado"));

        Usuario usuario = cliente.getUsuario();

        // Desactivación lógica
        usuario.setEstado(Estado.INACTIVO);
        usuarioRepository.save(usuario);
    }

    public ClienteResponse me(String correo) {

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() ->
                        new NoSuchElementException("Usuario no encontrado"));

        Cliente cliente = clienteRepository
                .findByUsuario(usuario)
                .orElseThrow(() ->
                        new NoSuchElementException("Cliente no encontrado"));

        return clienteMapper.toDto(cliente);
    }

    @Transactional
    public ClienteResponse actualizarMe(
            String correoActual,
            ClienteUpdateRequest request) {

        Usuario usuario = usuarioRepository.findByCorreo(correoActual)
                .orElseThrow(() ->
                        new NoSuchElementException("Usuario no encontrado"));

        Cliente cliente = clienteRepository.findByUsuario(usuario)
                .orElseThrow(() ->
                        new NoSuchElementException("Cliente no encontrado"));

        actualizarRuc(cliente, request.ruc());

        if (request.razonSocial() != null) {
            cliente.setRazonSocial(request.razonSocial());
        }

        if (request.direccion() != null) {
            cliente.setDireccion(request.direccion());
        }

        if (request.telefono() != null) {
            cliente.setTelefono(request.telefono());
        }

        if (request.correo() != null
                && !usuario.getCorreo().equals(request.correo())) {

            if (usuarioRepository
                    .findByCorreo(request.correo())
                    .isPresent()) {

                throw new IllegalArgumentException(
                        "El correo ya está registrado");
            }

            usuario.setCorreo(request.correo());
        }

        if (request.contraseña() != null
                && !request.contraseña().isBlank()) {

            usuario.setContraseña(
                    passwordEncoder.encode(request.contraseña())
            );
        }

        usuarioRepository.save(usuario);
        clienteRepository.save(cliente);

        return clienteMapper.toDto(cliente);
    }
}
