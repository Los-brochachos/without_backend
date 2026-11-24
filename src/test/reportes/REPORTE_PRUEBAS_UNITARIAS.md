# Reporte de pruebas unitarias

Proyecto: without Backend. Fecha de ejecucion: 18 de octubre de 2026.

## Objetivo y componentes seleccionados

Verificar metodos de dos servicios criticos con escenarios normales, alternativos
y limite, mediante pruebas automatizadas reproducibles.

1. **ProductoService**: administra el catalogo usado en los pedidos. Se prueban
   creacion, deteccion de duplicados, consulta, actualizacion parcial y baja logica.
2. **FallaService**: controla la progresion del mantenimiento de cisternas. Se prueban la creacion y
   el metodo `cambiarEstado`, que debe permitir solamente la siguiente etapa:
   `PENDIENTE -> EN_REPARACION -> RESUELTA`.

## Herramientas y aislamiento

Se utilizan JUnit Jupiter (JUnit 5), Mockito y Maven Surefire, disponibles mediante
las dependencias de prueba existentes del proyecto Spring Boot. No se modifica
`pom.xml` ni ningun archivo de `src/main`.

`@Test` identifica cada prueba, `@BeforeEach` prepara un servicio independiente,
`@DisplayName` describe el escenario y `@ExtendWith(MockitoExtension.class)`
habilita los repositorios simulados declarados con `@Mock`.

Los servicios y los mappers se ejecutan realmente; los repositorios se simulan.
Se comprueban respuestas, cambios de estado, excepciones y llamadas de guardado.
Estas son pruebas unitarias: no necesitan MySQL, servidor HTTP, perfil `dev`
ni carga de datos. No se usa `@SpringBootTest` porque cargaria el contexto completo
el proyecto ya tiene una prueba de contexto independiente.

Cada prueba sigue Arrange-Act-Assert: preparar entradas y respuestas simuladas,
ejecutar el metodo y comprobar el resultado. Los datos se reconstruyen para cada
prueba; no se depende del orden de ejecucion ni de valores de la base real.

## Casos y resultados

| ID | Metodo | Escenario | Entrada o condicion | Resultado esperado | Obtenido |
| --- | --- | --- | --- | --- | --- |
| P01 | crearProducto | Normal | Nombre nuevo, O2, m3, precio 8.50 | Guarda producto ACTIVO y devuelve ID y campos | Aprobado |
| P02 | crearProducto | Alternativo | Nombre ya registrado | IllegalArgumentException; no guarda | Aprobado |
| P03 | buscarPorId | Alternativo | ID 999 inexistente | NoSuchElementException con mensaje; no guarda | Aprobado |
| P04 | actualizarProducto | Normal | Solo precio 9.25; otros campos null | Cambia precio y conserva nombre, gas, unidad y estado | Aprobado |
| P05 | actualizarProducto | Limite | Todos los campos opcionales null | Conserva todos los valores; no busca duplicados | Aprobado |
| P06 | actualizarProducto | Limite | Nombre igual al actual | Acepta; no interpreta su propio nombre como duplicado | Aprobado |
| P07 | eliminarProducto | Normal | Producto activo existente | Guarda INACTIVO; no llama a delete ni deleteById | Aprobado |
| F01 | cambiarEstado | Normal | PENDIENTE -> EN_REPARACION | Guarda siguiente estado y devuelve datos de la falla | Aprobado |
| F02 | cambiarEstado | Normal | EN_REPARACION -> RESUELTA | Guarda estado final y devuelve datos de la falla | Aprobado |
| F03 | cambiarEstado | Alternativo | PENDIENTE -> RESUELTA | Rechaza salto; conserva estado y no guarda | Aprobado |
| F04 | cambiarEstado | Alternativo | EN_REPARACION -> PENDIENTE | Rechaza retroceso; conserva estado y no guarda | Aprobado |
| F05 | cambiarEstado | Limite | PENDIENTE -> PENDIENTE | Rechaza repeticion; conserva estado y no guarda | Aprobado |
| F06 | cambiarEstado | Limite | RESUELTA -> EN_REPARACION | Rechaza reapertura desde estado final; no guarda | Aprobado |
| F07 | cambiarEstado | Alternativo | ID 999 inexistente | NoSuchElementException con mensaje; no guarda | Aprobado |
| P08 | actualizarProducto | Alternativo | Nuevo nombre ocupado | Rechaza sin modificar campos ni guardar | Aprobado |
| P09 | actualizarProducto | Normal | Nuevo nombre disponible | Guarda nombre nuevo y conserva precio | Aprobado |
| P10 | actualizarProducto | Alternativo | ID inexistente | Rechaza sin buscar duplicados ni guardar | Aprobado |
| P11 | eliminarProducto | Alternativo | ID inexistente | Rechaza sin guardar ni borrar | Aprobado |
| F08 | crear | Normal | Usuario, tecnico y cisterna existentes | Guarda PENDIENTE y asigna relaciones correctas | Aprobado |
| F09 | crear | Alternativo | Usuario inexistente | Rechaza antes de consultar tecnico o guardar | Aprobado |
| F10 | crear | Alternativo | Usuario sin tecnico asociado | Rechaza sin consultar cisterna ni guardar | Aprobado |
| F11 | crear | Alternativo | Cisterna inexistente | Rechaza sin guardar | Aprobado |

Los limites elegidos son limites de las reglas de los servicios: actualizacion sin
campos, nombre sin cambio, ausencia de avance y estado terminal. La validacion
de precios, longitudes y campos obligatorios mediante anotaciones de los DTO es
otra capa; llamar directamente al servicio no ejecuta automaticamente `@Valid`.

## Ejecucion y evidencia

Desde la raiz del backend, en PowerShell:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.4"
.\mvnw.cmd -o test "-Dtest=ProductoServiceTest,FallaServiceTest"
```

`-o` usa las dependencias ya disponibles localmente. Si es la primera ejecucion y
faltan dependencias, ejecutar el mismo comando sin `-o`.

El selector `-Dtest` ejecuta solamente estas dos clases, evitando que la prueba
de contexto existente se conecte a la base de datos.

Resultado real de Maven Surefire:

```text
FallaService:    Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
ProductoService: Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
Total:          Tests run: 22, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Maven registro un tiempo total de 9.458 segundos. Las clases de fallas y productos
tardaron 1.137 y 0.099 segundos respectivamente. Estos tiempos pueden variar.
Las advertencias de instrumentacion de Mockito y Lombok no causaron fallos.

Se conservan copias de los reportes de esta ejecucion en `evidencias/`, junto a
este documento. Maven tambien genera los originales TXT y XML en
`target/surefire-reports`; esos originales se regeneran al ejecutar pruebas y se
eliminan con `mvn clean`.

## Relacion con TDD y alcance

Estas pruebas se escribieron sobre servicios existentes; no se presenta esta
ejecucion como un ciclo TDD Red-Green-Refactor. Sirven como base de regresion para
futuros cambios: definir una nueva regla mediante una prueba que falle, implementar
la regla hasta que pase y refactorizar conservando las pruebas aprobadas.

Los resultados verifican los escenarios descritos. No prueban HTTP, autorizacion,
validacion de controladores, SQL, transacciones reales ni cobertura completa de
los servicios. No se calculo un porcentaje de cobertura.

## Correspondencia con la lista de cotejo

| Criterio | Evidencia incluida | Puntos de la rubrica |
| --- | --- | --- |
| Seleccion adecuada de componentes | Justificacion de ProductoService y FallaService | 3 |
| Casos normales definidos | P01, P04, P07, F01 y F02 | 4 |
| Casos limite considerados | P05, P06, F05 y F06 | 4 |
| Pruebas implementadas correctamente | Dos clases con 22 pruebas, aserciones y verificaciones Mockito | 6 |
| Evidencias registradas | Tabla de resultados y reportes Surefire conservados | 3 |

La puntuacion final corresponde a la evaluacion del docente.

## Incidencias de la ejecucion actual

- La primera ejecucion no llego a ejecutar pruebas: Maven utilizaba Java 17 y
  el proyecto requiere Java 25 (`release version 25 not supported`). Se resolvio
  seleccionando el JDK 25.0.4 instalado mediante JAVA_HOME, solo para la sesion.
- Con Java 25, Surefire registro 22 pruebas aprobadas y BUILD SUCCESS. No se
  detectaron defectos en los escenarios seleccionados. Las advertencias de
  Lombok, Mockito y carga dinamica del agente no impidieron las pruebas.
- PowerShell representa algunas advertencias de stderr como NativeCommandError;
  el resultado de pruebas se verifica en los XML/TXT de Surefire y BUILD SUCCESS.
- Se reutilizaron las 22 pruebas existentes y se volvieron a ejecutar; no se
  afirma que hayan sido creadas en esta revision ni que se haya aplicado TDD.
- Log completo: evidencias/ejecucion-unitarias-2026-10-18.log.