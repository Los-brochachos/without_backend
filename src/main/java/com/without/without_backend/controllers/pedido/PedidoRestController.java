package com.without.without_backend.controllers.pedido;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.without.without_backend.dto.comun.PaginaResponse;
import com.without.without_backend.dto.pedido.seguimientopedido.CancelarPedidoRequest;
import com.without.without_backend.dto.pedido.pedido.PedidoRequest;
import com.without.without_backend.dto.pedido.pedido.PedidoResponse;
import com.without.without_backend.dto.pedido.seguimientopedido.CambioEstadoPedidoRequest;
import com.without.without_backend.services.pedido.PedidoService;
import com.without.without_backend.utils.pedido.EstadoPedido;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Pedidos", description = "Registro, consulta (incluida paginada), cambio de estado y cancelación de pedidos.")
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
@Validated
@ApiResponse(responseCode = "401", description = "Sin token o token vencido", content = @Content)
@ApiResponse(responseCode = "403", description = "El rol no tiene permiso para esta operación", content = @Content)
public class PedidoRestController {

    private static final String EJEMPLO_PAGINA = """
        {
          "contenido": [
            {"idPedido": 25, "fechaRegistro": "2026-11-10", "estado": "RECIBIDO",
             "fechaEntregaEstimada": "2026-11-15", "prioridad": "ALTA",
             "idCliente": 3, "razonSocial": "Clínica Norte S.A.C."}
          ],
          "pagina": 0, "tamanio": 10, "totalElementos": 23, "totalPaginas": 3,
          "primera": true, "ultima": false
        }""";

    private final PedidoService service;

    @Operation(summary = "Listar todos los pedidos (sin paginar)",
        description = "Roles: ADMIN, ANALISTA, PROGRAMADOR. Para listados grandes use GET /api/v1/pedidos/paginado.")
    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @Operation(summary = "Listar pedidos paginados",
        description = "Roles: ADMIN, ANALISTA, PROGRAMADOR. La paginación y el filtro por estado se resuelven en la "
            + "base de datos (LIMIT/OFFSET + COUNT). Orden: más recientes primero (idPedido descendente).")
    @ApiResponse(responseCode = "200", description = "Página de pedidos",
        content = @Content(examples = @ExampleObject(EJEMPLO_PAGINA)))
    @ApiResponse(responseCode = "400", description = "pagina < 0, tamanio fuera de 1..50 o estado inexistente", content = @Content)
    @GetMapping("/paginado")
    public ResponseEntity<PaginaResponse<PedidoResponse>> listarPaginado(
            @Parameter(description = "Número de página, empieza en 0", example = "0")
            @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Registros por página (1 a 50)", example = "10")
            @RequestParam(defaultValue = "10") int tamanio,
            @Parameter(description = "Filtrar por estado del pedido (opcional)")
            @RequestParam(required = false) EstadoPedido estado) {

        return ResponseEntity.ok(service.listarPaginado(pagina, tamanio, estado));
    }

    @Operation(summary = "Listar mis pedidos paginados",
        description = "Rol: CLIENTE. Solo devuelve pedidos del cliente autenticado (el correo se toma del JWT).")
    @ApiResponse(responseCode = "200", description = "Página de pedidos del cliente",
        content = @Content(examples = @ExampleObject(EJEMPLO_PAGINA)))
    @ApiResponse(responseCode = "400", description = "pagina < 0, tamanio fuera de 1..50 o estado inexistente", content = @Content)
    @GetMapping("/mis-pedidos/paginado")
    public ResponseEntity<PaginaResponse<PedidoResponse>> listarMisPedidosPaginado(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "Número de página, empieza en 0", example = "0")
            @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Registros por página (1 a 50)", example = "10")
            @RequestParam(defaultValue = "10") int tamanio,
            @Parameter(description = "Filtrar por estado del pedido (opcional)")
            @RequestParam(required = false) EstadoPedido estado) {

        return ResponseEntity.ok(
                service.listarMisPedidosPaginado(authentication.getName(), pagina, tamanio, estado));
    }

    @Operation(summary = "Listar mis pedidos (sin paginar)", description = "Rol: CLIENTE.")
    @GetMapping("/mis-pedidos")
    public ResponseEntity<List<PedidoResponse>> listarMisPedidos(
            @Parameter(hidden = true) Authentication authentication) {

        return ResponseEntity.ok(
                service.listarMisPedidos(authentication.getName())
        );
    }

    @Operation(summary = "Consultar uno de mis pedidos", description = "Rol: CLIENTE. 404 si el pedido no es suyo.")
    @ApiResponse(responseCode = "404", description = "Pedido inexistente o de otro cliente", content = @Content)
    @GetMapping("/mis-pedidos/{id}")
    public ResponseEntity<PedidoResponse> buscarMiPedido(
            @PathVariable Integer id,
            @Parameter(hidden = true) Authentication authentication) {

        return ResponseEntity.ok(
                service.buscarMiPedido(
                        id,
                        authentication.getName()
                )
        );
    }

    @Operation(summary = "Cancelar uno de mis pedidos",
        description = "Rol: CLIENTE. Solo pedidos propios que aún no han sido despachados.")
    @ApiResponse(responseCode = "400", description = "El pedido ya no se puede cancelar", content = @Content)
    @ApiResponse(responseCode = "404", description = "Pedido inexistente o de otro cliente", content = @Content)
    @PatchMapping("/mis-pedidos/{id}/cancelar")
    public ResponseEntity<PedidoResponse> cancelarMiPedido(
            @PathVariable Integer id,
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody CancelarPedidoRequest request) {

        return ResponseEntity.ok(
                service.cancelarMiPedido(
                        id,
                        request,
                        authentication.getName()
                )
        );
    }

    @Operation(summary = "Consultar un pedido por ID", description = "Roles: ADMIN, ANALISTA, PROGRAMADOR.")
    @ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content)
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> buscarPorId(
            @PathVariable Integer id) {

        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @Operation(summary = "Registrar pedido",
        description = "Rol: CLIENTE. Crea el pedido en estado RECIBIDO con sus detalles y el primer registro de "
            + "seguimiento. No se permiten productos repetidos ni inactivos.")
    @ApiResponse(responseCode = "201", description = "Pedido creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos, producto repetido o inactivo", content = @Content)
    @PostMapping
    public ResponseEntity<PedidoResponse> insertar(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody PedidoRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.insertar(request,authentication.getName()));
    }

    @Operation(summary = "Cambiar estado del pedido",
        description = "Rol: PROGRAMADOR. Solo avanza al estado siguiente: RECIBIDO → EN_PREPARACION → DESPACHADO → ENTREGADO.")
    @ApiResponse(responseCode = "400", description = "Transición de estado no permitida", content = @Content)
    @ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content)
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoResponse> cambiarEstado(
            @PathVariable Integer id,
            @Valid @RequestBody CambioEstadoPedidoRequest request) {

        return ResponseEntity.ok(
                service.cambiarEstado(id, request)
        );
    }

    @Operation(summary = "Cancelar pedido", description = "Roles: ADMIN, PROGRAMADOR. Requiere observación.")
    @ApiResponse(responseCode = "400", description = "El pedido ya no se puede cancelar", content = @Content)
    @ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content)
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<PedidoResponse> cancelar(
            @PathVariable Integer id,
            @Valid @RequestBody CancelarPedidoRequest request) {

        return ResponseEntity.ok(
                service.cancelar(id, request)
        );
    }
}
