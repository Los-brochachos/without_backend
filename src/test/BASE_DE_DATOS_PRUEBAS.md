# MySQL de pruebas

El perfil `test` está en `src/test/resources/application-test.properties` y apunta a `withoutdb_test` en MySQL local (puerto 3306). Solo las dos pruebas `@SpringBootTest` lo activan. No modifica la configuración de producción.

## Tablas

Hibernate crea las tablas desde las entidades al iniciar estas pruebas (`create-drop`) y las elimina al cerrar el contexto. Por tanto, las tablas no quedan disponibles después: esto es intencional. No activar `dev` ni el seeder para preparar esta base. No se copian datos de desarrollo.

Por autorización del usuario, se usan las credenciales locales de `src/main/resources/application.properties`, sin copiarlas al perfil de pruebas. No hacen falta variables TEST_DB_USER/TEST_DB_PASSWORD. La comprobación previa solo admite `localhost:3306/withoutdb_test`, sin parámetros JDBC ni perfiles dev/prod. El usuario actual puede tener permisos sobre otras bases; el aislamiento de esta ejecución se comprueba en la configuración y en el inicializador, no mediante un usuario limitado.

No publicar credenciales ni incluirlas en capturas. La conexión de la aplicación habitual no se modifica.

## Ejecucion posterior

Después de verificar la conexión de pruebas:

```powershell
.\mvnw.cmd "-Dtest=AuthPersistenceTest,WithoutBackendApplicationTests" verify
```

Este comando sí crea y elimina tablas de `withoutdb_test`. No ejecutar dos procesos a la vez sobre esta base: el cierre de uno podría eliminar tablas usadas por el otro. El resto de la suite todavía contiene un defecto funcional conocido de detalle de pedido y no se ha corregido.

La barrera de configuración se prueba separadamente sin MySQL con `TestDatabaseInitializerTest`.

## Ejecución verificada del 10/11/2026

Se verificó la conexión a `withoutdb_test` con cero tablas iniciales. Se ejecutaron AuthPersistenceTest, WithoutBackendApplicationTests y TestDatabaseInitializerTest: **20 pruebas aprobadas**, sin fallos ni errores. Hibernate creó las tablas y las eliminó al terminar; una consulta posterior confirmó cero tablas. No se activó el seeder. JaCoCo midió 438/2667 líneas (16,42 %) y 21/522 ramas (4,02 %) de producción para esta selección limitada. No es cobertura de la suite completa.

Reporte conservado: `evidencia-calidad/punto-3/jacoco-backend-2026-11-10/index.html`. Log y alcance: `evidencia-calidad/punto-3/INTEGRACION_MYSQL_JACOCO_2026-11-10.md`.
