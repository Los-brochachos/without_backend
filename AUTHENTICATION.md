# Autenticacion para Angular

El backend ahora devuelve un access token de 15 minutos en JSON y envia un
refresh token de 30 dias en la cookie `without_refresh`. Angular debe conservar
el access en memoria. El refresh no aparece en JSON ni se lee desde JavaScript.

## Contrato y orden de llamadas

Todas las llamadas a `/auth` desde Angular deben usar `withCredentials: true`
para que el navegador acepte y envie las cookies al backend de otro origen.

1. **GET /auth/csrf**, sin Bearer. Devuelve `{token, headerName}` y establece
   la cookie CSRF. Guardar ambos valores en memoria. El token es apto para enviar
   como encabezado; no leer la cookie CSRF ni intentar deshacer su enmascaramiento.
2. **POST /auth/login** con JSON `{correo, contraseña}`. Enviar el encabezado
   indicado por `headerName` (actualmente `X-XSRF-TOKEN`) con el valor `token`.
   Devuelve `{access_token}` y establece la cookie HttpOnly de refresh.
3. **GET /api/v1/usuarios/me** con `Authorization: Bearer <access_token>` para
   obtener el perfil y el rol. Los demas endpoints de negocio tambien usan Bearer.
4. **POST /auth/refresh-token**, sin refresh en el cuerpo y sin necesitar Bearer.
   Enviar cookies y encabezado CSRF. Devuelve otro `{access_token}` y reemplaza
   la cookie de refresh. El refresh anterior deja de ser valido.
5. **POST /auth/logout**, con cookies y encabezado CSRF. Revoca el refresh actual
   y elimina su cookie. Devuelve 204. Angular debe limpiar tambien su estado local.

Al recargar Angular: pedir CSRF, intentar refresh y luego consultar el perfil.
Si refresh devuelve 401, volver al login. Si CSRF falla, Spring devuelve 403.
Un 403 en una API de negocio tambien puede significar permisos insuficientes.
Angular debe centralizar las renovaciones para no enviar dos refresh simultaneos
con la misma cookie; uno de ellos sera rechazado despues de la rotacion.

No enviar el Bearer viejo a `/auth`; estos endpoints usan su propio flujo.
El interceptor debe limitar el envio del access token a la API de este backend.
Angular no envia correo o ID del cliente para consultar sus propios pedidos:
el backend obtiene esa identidad desde el access token autenticado.

Las respuestas de autenticacion llevan `Cache-Control: no-store`.
El backend usa errores genericos 401 para no revelar detalles de autenticacion.

## Cookies y CSRF

`without_refresh`: `HttpOnly`, `Path=/auth`, `SameSite=Strict` y `Secure` por defecto.
No se configura Domain. La cookie CSRF tambien es HttpOnly y se limita a `/auth`;
el frontend obtiene el token CSRF desde el cuerpo de GET `/auth/csrf`.

Se usa CookieCsrfTokenRepository con el enmascaramiento predeterminado de Spring.
Login, refresh y logout requieren CSRF. Las APIs de negocio no autentican mediante
cookies: solo aceptan Bearer y conservan sus restricciones de roles.

Los origenes CORS son concretos y se permite el envio de credenciales. Para
despliegue, configurar `application.auth.allowed-origins` con una lista separada
por comas de los origenes reales. El valor por defecto conserva los localhost
anteriores. SameSite se configura con `application.auth.cookie-same-site`.
Strict funciona para frontend/backend del mismo sitio; dominios de sitios distintos
requieren revisar el despliegue, las restricciones de cookies del navegador y CSRF.
No cambiar a None sin HTTPS y sin mantener la proteccion CSRF.

## Desarrollo y produccion

Desarrollo local por HTTP:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

El perfil dev permite cookies sin Secure exclusivamente para trabajar en HTTP.
Tambien activa el seeder existente, que omite la carga si hay datos.

El secreto ahora se obtiene de `JWT_SECRET`. Si no se configura, fuera del perfil
prod se genera una clave criptografica efimera: reiniciar invalida las sesiones.
Para conservarlas entre reinicios, configurar un secreto aleatorio estable de
al menos 32 bytes, guardado fuera del repositorio, y compartirlo entre instancias.

El perfil prod exige `JWT_SECRET` y mantiene Secure habilitado. Desplegar con
HTTPS y orígenes CORS reales. Las credenciales de base de datos de desarrollo
siguen en su configuracion existente; deben reemplazarse para despliegue.

## Persistencia, rotacion y limites

Solo se registra el hash SHA-256 del refresh, su propietario, tipo y vencimiento.
No se guarda el access token. Una transaccion consume el refresh registrado y
guarda el nuevo; la consulta usa bloqueo pesimista para serializar el consumo.
Se valida firma, vencimiento, tipo `token_use`, usuario activo y propietario.
El identificador JWT aleatorio hace que emisiones consecutivas sean diferentes.

Un refresh no puede autenticar `/api/v1`, ni un access puede renovar la sesion.
Un refresh consumido o revocado se rechaza. No hay revocacion de toda la familia
de sesiones cuando se detecta reutilizacion: se rechaza el token anterior.

Logout revoca el refresh de ese navegador. Un access ya emitido puede seguir
funcionando hasta vencer (15 minutos), salvo que el usuario se desactive. No se
afirma revocacion inmediata de access tokens ni cierre de todos los dispositivos.

Los tokens anteriores no incluyen el tipo requerido y no se aceptan en este flujo.
Los registros antiguos en la tabla token no se borran automaticamente ni se usan
para renovar. Los usuarios deben iniciar sesion nuevamente.

HttpOnly reduce el robo del refresh por JavaScript; no evita que XSS realice
peticiones desde la pagina. Mantener sanitizacion, dependencias actualizadas y
limitar intentos de login en la infraestructura antes de exponer el sistema.

## Verificacion

```powershell
.\mvnw.cmd -o test "-Dtest=JwtServiceTest,TokenServiceTest,AuthServiceTest,AuthSecurityTest,AuthPersistenceTest"
```

25 pruebas aprobadas: tipos JWT, firma y expiracion, hash del refresh, rechazo de
reutilizacion, rotacion, logout, cookies, CSRF y contrato HTTP. AuthSecurityTest
usa Spring Security y MockMvc con dependencias simuladas; AuthPersistenceTest
usa MySQL configurado con `ddl-auto=validate` y revierte sus datos al terminar.
No se midio concurrencia real entre dos transacciones independientes ni se
probo el comportamiento de cookies en un navegador de produccion.

La suite general conserva el fallo documentado previamente en PedidoServiceTest:
el estado del detalle nuevo es null. Este cambio de autenticacion no lo corrige.

Referencias:
- https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html
- https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Set-Cookie
- https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html
