# Pruebas adicionales de pedidos y seguimiento

Fecha: 4 de octubre de 2026. Solo se agregan archivos en `src/test`.

## Componentes

- `PedidoService`: registro con detalles, avance secuencial de estados,
  cancelacion y pertenencia del pedido al cliente.
- `SeguimientoPedidoService`: consulta de historial y comprobacion de que
  el pedido consultado corresponde al cliente.

JUnit 5, Mockito y mappers reales; repositorios simulados. No se inicia Spring
ni MySQL. Las pruebas parametrizadas ejecutan un caso independiente por entrada.

## Escenarios

| ID | Escenario | Casos ejecutados | Resultado |
| --- | --- | --- | --- |
| PD01 | Registro de pedido, detalle con precio vigente y seguimiento RECIBIDO | 1 | Fallo: estado del detalle null, esperado ACTIVO |
| PD02 | Rechazo de productos repetidos antes de guardar | 1 | Aprobado |
| PD03 | Rechazo de producto inactivo sin guardar detalle ni seguimiento | 1 | Aprobado |
| PD04 | Tres avances validos hasta ENTREGADO, con seguimiento | 3 | Aprobados |
| PD05 | Rechazo de salto, retroceso, repeticion, cancelacion por cambio de estado y salida de estados terminales | 6 | Aprobados |
| PD06 | Cancelacion con registro de motivo y estado | 1 | Aprobado |
| PD07 | Rechazo de cancelacion de pedido ENTREGADO o CANCELADO | 2 | Aprobados |
| PD08 | Cambio de estado de pedido inexistente | 1 | Aprobado |
| PD09 | Cliente intenta cancelar pedido ajeno o inexistente | 1 | Aprobado |
| S01 | Consulta del historial con consulta cronologica y conversion a DTO | 1 | Aprobado |
| S02 | Pedido existente con historial vacio | 1 | Aprobado |
| S03 | Pedido inexistente no consulta historial | 1 | Aprobado |
| S04 | Cliente consulta historial de pedido propio | 1 | Aprobado |
| S05 | Pedido ajeno o inexistente no revela historial | 1 | Aprobado |
| S06 | Usuario inexistente impide consultar | 1 | Aprobado |
| S07 | Usuario sin cliente asociado impide consultar | 1 | Aprobado |

## Defecto encontrado por PD01

El metodo `PedidoService.insertar` llama a `DetallePedidoMapper.toEntity` y luego
guarda el detalle. El mapper asigna cantidad, precio, pedido y producto, pero no
asigna estado. El servicio tampoco lo asigna antes de guardar. La entidad
`DetallePedido` declara el estado con `@Column(nullable = false)` y no tiene
valor predeterminado ni callback que lo complete.

El mock del repositorio permite capturar el objeto enviado a `save`; la asercion
detecta que su estado es null. Esto identifica una violacion de una restriccion
de la entidad, aunque esta prueba no ejecuta una insercion real en MySQL.

```text
PedidoServiceTest.insertarPedidoCreaDetalleYSeguimiento
expected: <ACTIVO> but was: <null>
```

Correccion propuesta para cuando se autorice modificar `src/main`: establecer
`Estado.ACTIVO` al construir el detalle nuevo o antes de guardarlo. La prueba
permanece activa y fallando para evidenciar el defecto. No se cambia su expectativa,
no se deshabilita y no se modifica el codigo de produccion.

## Ejecucion y resultados reales

```powershell
.\mvnw.cmd -o test "-Dtest=ProductoServiceTest,FallaServiceTest,PedidoServiceTest,SeguimientoPedidoServiceTest"
```

| Servicio | Pruebas | Aprobadas | Fallos | Errores | Omitidas |
| --- | --- | --- | --- | --- | --- |
| ProductoService | 11 | 11 | 0 | 0 | 0 |
| FallaService | 11 | 11 | 0 | 0 | 0 |
| PedidoService | 17 | 16 | 1 | 0 | 0 |
| SeguimientoPedidoService | 7 | 7 | 0 | 0 | 0 |
| Total | 46 | 45 | 1 | 0 | 0 |

Se agregaron 24 casos: 23 aprobados y uno que detecta el defecto descrito.
Maven termino con `BUILD FAILURE` por esa asercion. Los 22 casos de productos y
fallas siguen aprobados. Las evidencias de esta ejecucion conjunta estan en
`evidencias/pedidos-y-seguimiento/`, en formato TXT y XML de Maven Surefire.
El reporte anterior conserva las evidencias de la ejecucion anterior de los
dos primeros servicios; este documento registra la ampliacion.

## Alcance y limites

Se verifica el comportamiento en memoria y las llamadas a repositorios.
El orden cronologico del historial lo solicita el servicio al repositorio;
esta prueba comprueba la consulta utilizada y conserva el orden recibido,
pero no demuestra que la consulta SQL ordene realmente.

En PD03 el pedido se guarda antes de validar el producto. El test comprueba que
no se guardan detalles ni seguimiento; no afirma que no se guarde el pedido.
El rollback de `@Transactional` requiere una prueba de integracion con Spring
y base de datos; no puede demostrarse con estos mocks.

En cambios de estado el servicio modifica una entidad existente sin llamar
explicitamente a `save` del pedido. Las pruebas comprueban la mutacion y el
seguimiento. La persistencia mediante dirty checking requiere integracion.

Los casos de pertenencia prueban la comprobacion realizada por el servicio;
no comprueban permisos HTTP o roles de Spring Security. No se midio cobertura.

## Actualizacion: correccion verificada el 18 de octubre de 2026

Los resultados anteriores son historicos. Se reprodujo PD01 con el fallo
`expected: <ACTIVO> but was: <null>`. Se corrigio PedidoService.insertar para
asignar `detalle.setEstado(Estado.ACTIVO)` antes de guardar cada detalle.
La asercion original se conserva sin cambios.

Se ejecuto con JAVA_HOME apuntando al JDK 25.0.4 instalado:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.4"
.\mvnw.cmd -o clean test "-Dtest=PedidoServiceTest,SeguimientoPedidoServiceTest,ProductoServiceTest,FallaServiceTest"
```

La recompilacion limpia tambien resolvio un VerifyError del bytecode generado
que aparecio en el intento incremental.

Resultado actual: PedidoService 17/17, SeguimientoPedidoService 7/7,
ProductoService 11/11 y FallaService 11/11. Total: 46 pruebas, 0 fallos,
0 errores, 0 omitidas. BUILD SUCCESS; tiempo total 12.104 segundos.
Evidencias actuales: `evidencias/correccion-pedido-2026-10-18/`.
