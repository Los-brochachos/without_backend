# Datos de desarrollo

`DatabaseSeeder` se ejecuta solo con el perfil `dev`. Inserta en una transaccion
datos ficticios para las 17 tablas de negocio. Si cualquiera ya contiene registros,
omite toda la carga. No borra ni modifica registros existentes. Si una insercion
falla, se revierte la transaccion; los contadores AUTO_INCREMENT pueden avanzar.

Desde PowerShell, con MySQL configurado en `application.properties`:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

En el IDE tambien puedes agregar `--spring.profiles.active=dev` a los argumentos
de la aplicacion. Usa una base de desarrollo vacia con el esquema actual.

## Cuentas

Todas usan la clave de desarrollo `without1234*`, almacenada con el PasswordEncoder
del proyecto (BCrypt). Los correos son ficticios:

| Rol | Correo |
| --- | --- |
| ADMIN | admin@without.example |
| ANALISTA | analista@without.example |
| CONDUCTOR | conductor1@without.example a conductor5@without.example |
| PROGRAMADOR | programador1@without.example a programador5@without.example |
| TECNICO | tecnico1@without.example a tecnico5@without.example |
| CLIENTE | cliente1@without.example a cliente5@without.example |

Se crean 178 registros: 22 usuarios, 17 trabajadores, 5 conductores, 5 programadores,
5 tecnicos, 5 clientes, 5 cisternas, 5 productos, 15 pedidos, 15 detalles,
36 seguimientos, 9 atenciones, 9 alertas, 6 facturas, 6 guias, 8 fallas
y 5 historiales de mantenimiento. Cada tabla de negocio recibe al menos 5 registros.
Los IDs los genera JPA; no se fijan manualmente.
Las fechas operativas se calculan respecto al dia de ejecucion en America/Lima.
`token` se llena mediante el login real, no con tokens ficticios.

## Diferencias con el dump compartido

El dump no coincide con las entidades actuales:

- `Falla` usa `id_tecnico`, mientras el dump exige `id_conductor`.
- `HistorialMantenimiento` solo referencia `id_falla`; el dump tambien exige
  `id_cisterna` e `id_tecnico`.
- `Programador` ya no tiene `area`, pero el dump la exige.
- `Tecnico` ya no tiene `certificacion`, pero el dump la exige.

`ddl-auto=update` no garantiza eliminar columnas antiguas ni sus restricciones.
Si conservas el esquema del dump, debes migrarlo para reflejar las entidades
actuales antes de ejecutar la carga. Otra opcion es apuntar a una nueva base de
desarrollo vacia para que Hibernate cree las tablas actuales. No ejecutes el dump
antiguo sobre esa base. El seeder no cambia el esquema.

`src/main/resources/datawithout.sql` es un script anterior independiente; no es
necesario ejecutarlo junto con este seeder.
