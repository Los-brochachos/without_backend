USE withoutdb;

START TRANSACTION;

-- =========================================================
-- 1. USUARIO
-- =========================================================

INSERT INTO usuario
(id_usuario, correo, contraseña, estado, rol)
VALUES
(1,  'cliente1@without.com',     '$2a$10$z3njX9A5cqUax4LTn18vgO/7mY69Xk21sK8ekdFctpds96obpDyX2', 'ACTIVO', 'CLIENTE'),
(2,  'cliente2@without.com',     '$2a$10$z3njX9A5cqUax4LTn18vgO/7mY69Xk21sK8ekdFctpds96obpDyX2', 'ACTIVO', 'CLIENTE'),
(3,  'cliente3@without.com',     '$2a$10$z3njX9A5cqUax4LTn18vgO/7mY69Xk21sK8ekdFctpds96obpDyX2', 'ACTIVO', 'CLIENTE'),
(4,  'cliente4@without.com',     '$2a$10$z3njX9A5cqUax4LTn18vgO/7mY69Xk21sK8ekdFctpds96obpDyX2', 'ACTIVO', 'CLIENTE'),
(5,  'cliente5@without.com',     '$2a$10$z3njX9A5cqUax4LTn18vgO/7mY69Xk21sK8ekdFctpds96obpDyX2', 'ACTIVO', 'CLIENTE'),

(6,  'conductor1@without.com',   '$2a$10$z3njX9A5cqUax4LTn18vgO/7mY69Xk21sK8ekdFctpds96obpDyX2', 'ACTIVO', 'CONDUCTOR'),
(7,  'conductor2@without.com',   '$2a$10$z3njX9A5cqUax4LTn18vgO/7mY69Xk21sK8ekdFctpds96obpDyX2', 'ACTIVO', 'CONDUCTOR'),
(8,  'conductor3@without.com',   '$2a$10$z3njX9A5cqUax4LTn18vgO/7mY69Xk21sK8ekdFctpds96obpDyX2', 'ACTIVO', 'CONDUCTOR'),
(9,  'conductor4@without.com',   '$2a$10$z3njX9A5cqUax4LTn18vgO/7mY69Xk21sK8ekdFctpds96obpDyX2', 'ACTIVO', 'CONDUCTOR'),
(10, 'conductor5@without.com',   '$2a$10$z3njX9A5cqUax4LTn18vgO/7mY69Xk21sK8ekdFctpds96obpDyX2', 'ACTIVO', 'CONDUCTOR'),

(11, 'tecnico1@without.com',     '$2a$10$z3njX9A5cqUax4LTn18vgO/7mY69Xk21sK8ekdFctpds96obpDyX2', 'ACTIVO', 'TECNICO'),
(12, 'tecnico2@without.com',     '$2a$10$z3njX9A5cqUax4LTn18vgO/7mY69Xk21sK8ekdFctpds96obpDyX2', 'ACTIVO', 'TECNICO'),
(13, 'tecnico3@without.com',     '$2a$10$z3njX9A5cqUax4LTn18vgO/7mY69Xk21sK8ekdFctpds96obpDyX2', 'ACTIVO', 'TECNICO'),
(14, 'tecnico4@without.com',     '$2a$10$z3njX9A5cqUax4LTn18vgO/7mY69Xk21sK8ekdFctpds96obpDyX2', 'ACTIVO', 'TECNICO'),
(15, 'tecnico5@without.com',     '$2a$10$z3njX9A5cqUax4LTn18vgO/7mY69Xk21sK8ekdFctpds96obpDyX2', 'ACTIVO', 'TECNICO'),

(16, 'programador1@without.com', '$2a$10$z3njX9A5cqUax4LTn18vgO/7mY69Xk21sK8ekdFctpds96obpDyX2', 'ACTIVO', 'PROGRAMADOR'),
(17, 'programador2@without.com', '$2a$10$z3njX9A5cqUax4LTn18vgO/7mY69Xk21sK8ekdFctpds96obpDyX2', 'ACTIVO', 'PROGRAMADOR'),
(18, 'programador3@without.com', '$2a$10$z3njX9A5cqUax4LTn18vgO/7mY69Xk21sK8ekdFctpds96obpDyX2', 'ACTIVO', 'PROGRAMADOR'),
(19, 'programador4@without.com', '$2a$10$z3njX9A5cqUax4LTn18vgO/7mY69Xk21sK8ekdFctpds96obpDyX2', 'ACTIVO', 'PROGRAMADOR'),
(20, 'programador5@without.com', '$2a$10$z3njX9A5cqUax4LTn18vgO/7mY69Xk21sK8ekdFctpds96obpDyX2', 'ACTIVO', 'PROGRAMADOR'),

(21, 'admin@without.com',        '$2a$10$z3njX9A5cqUax4LTn18vgO/7mY69Xk21sK8ekdFctpds96obpDyX2', 'ACTIVO', 'ADMIN');


-- =========================================================
-- 2. CLIENTE
-- =========================================================

INSERT INTO cliente
(id_cliente, id_usuario, ruc, telefono, correo, razon_social, direccion)
VALUES
(1, 1, '20123456781', '987654321', 'cliente1@without.com',
 'Hospital San Marcos S.A.C.', 'Av. Universitaria 1001, Lima'),

(2, 2, '20234567892', '987654322', 'cliente2@without.com',
 'Clinica Vida Salud S.A.C.', 'Av. Brasil 1250, Lima'),

(3, 3, '20345678903', '987654323', 'cliente3@without.com',
 'Industrias del Pacifico S.A.', 'Av. Argentina 2200, Lima'),

(4, 4, '20456789014', '987654324', 'cliente4@without.com',
 'Laboratorios Andinos S.A.C.', 'Av. Arequipa 1800, Lima'),

(5, 5, '20567890125', '987654325', 'cliente5@without.com',
 'Metalurgica Nacional S.A.', 'Av. Nestor Gambetta 1500, Callao');


-- =========================================================
-- 3. TRABAJADOR
-- =========================================================

INSERT INTO trabajador
(id_trabajador, fecha_ingreso, id_usuario, dni, estado, telefono,
 apellidos, nombres, direccion)
VALUES
(1,  '2023-01-15', 6,  '70123456', 'ACTIVO', '999111111',
 'Perez Gomez', 'Carlos', 'Av. Central 101, Lima'),

(2,  '2023-03-20', 7,  '70234567', 'ACTIVO', '999111112',
 'Ramirez Torres', 'Luis', 'Av. Norte 202, Lima'),

(3,  '2022-06-10', 8,  '70345678', 'ACTIVO', '999111113',
 'Flores Diaz', 'Miguel', 'Av. Sur 303, Lima'),

(4,  '2024-01-08', 9,  '70456789', 'ACTIVO', '999111114',
 'Castillo Ruiz', 'Jose', 'Av. Los Olivos 404, Lima'),

(5,  '2024-04-18', 10, '70567890', 'ACTIVO', '999111115',
 'Quispe Soto', 'Jorge', 'Av. Peru 505, Lima'),

(6,  '2022-02-14', 11, '70678901', 'ACTIVO', '999222221',
 'Huaman Perez', 'Andrea', 'Av. Industrial 606, Lima'),

(7,  '2023-05-11', 12, '70789012', 'ACTIVO', '999222222',
 'Mendoza Rojas', 'Patricia', 'Av. Lima 707, Lima'),

(8,  '2024-02-19', 13, '70890123', 'ACTIVO', '999222223',
 'Vargas Leon', 'Daniel', 'Av. Colonial 808, Callao'),

(9,  '2023-08-22', 14, '70901234', 'ACTIVO', '999222224',
 'Salazar Cruz', 'Fernando', 'Av. Grau 909, Lima'),

(10, '2024-06-17', 15, '71012345', 'ACTIVO', '999222225',
 'Chavez Molina', 'Ricardo', 'Av. Tacna 1000, Lima'),

(11, '2022-01-10', 16, '71123456', 'ACTIVO', '999333331',
 'Torres Silva', 'Alejandro', 'Av. Primavera 110, Lima'),

(12, '2022-09-05', 17, '71234567', 'ACTIVO', '999333332',
 'Garcia Paredes', 'Diego', 'Av. San Luis 220, Lima'),

(13, '2023-07-03', 18, '71345678', 'ACTIVO', '999333333',
 'Morales Vega', 'Christian', 'Av. Canada 330, Lima'),

(14, '2024-03-12', 19, '71456789', 'ACTIVO', '999333334',
 'Ramos Herrera', 'Sergio', 'Av. Javier Prado 440, Lima'),

(15, '2024-05-25', 20, '71567890', 'ACTIVO', '999333335',
 'Espinoza Castro', 'Martin', 'Av. Angamos 550, Lima');


-- =========================================================
-- 4. CONDUCTOR
-- =========================================================

INSERT INTO conductor
(id_trabajador, categoria_licencia, licencia_conducir,
 fecha_vencimiento_licencia)
VALUES
(1, 'A-III-C', 'L00123456', '2028-01-15'),
(2, 'A-III-C', 'L00123457', '2028-03-20'),
(3, 'A-III-B', 'L00123458', '2027-06-10'),
(4, 'A-III-C', 'L00123459', '2029-01-08'),
(5, 'A-III-C', 'L00123460', '2029-04-18');


-- =========================================================
-- 5. TECNICO
-- =========================================================

INSERT INTO tecnico
(id_trabajador, nivel_tecnico, certificacion, especialidad)
VALUES
(6, 'Senior', 'Certificacion Mantenimiento 01',
 'Mecanica de cisternas'),

(7, 'Junior', 'Certificacion Mantenimiento 02',
 'Sistemas hidraulicos'),

(8, 'Senior', 'Certificacion Mantenimiento 03',
 'Sistemas electricos'),

(9, 'Senior', 'Certificacion Mantenimiento 04',
 'Mantenimiento industrial'),

(10, 'Junior', 'Certificacion Mantenimiento 05',
 'Diagnostico de vehiculos');


-- =========================================================
-- 6. PROGRAMADOR
-- =========================================================

INSERT INTO programador
(id_trabajador, turno, area)
VALUES
(11, 'Mañana', 'Programacion logistica'),
(12, 'Tarde', 'Planificacion de pedidos'),
(13, 'Noche', 'Monitoreo operativo'),
(14, 'Mañana', 'Asignacion de atenciones'),
(15, 'Tarde', 'Coordinacion de distribucion');


-- =========================================================
-- 7. CISTERNA
-- =========================================================

INSERT INTO cisterna
(id_cisterna, capacidad, estado, placa, nombre)
VALUES
(1, 12000.00, 'DISPONIBLE', 'ABC-101', 'Cisterna Oxigeno 01'),
(2, 15000.00, 'DISPONIBLE', 'ABC-102', 'Cisterna Oxigeno 02'),
(3, 10000.00, 'MANTENIMIENTO', 'ABC-103', 'Cisterna Nitrogeno 01'),
(4, 18000.00, 'DISPONIBLE', 'ABC-104', 'Cisterna Industrial 01'),
(5, 14000.00, 'DISPONIBLE', 'ABC-105', 'Cisterna Medicinal 01');


-- =========================================================
-- 8. PRODUCTO
-- =========================================================

INSERT INTO producto
(id_producto, precio_unitario, unidad_medida, tipo_gas, nombre, estado)
VALUES
(1, 150.00, 'm3', 'OXIGENO', 'Oxigeno Medicinal', 'ACTIVO'),
(2, 180.00, 'm3', 'OXIGENO', 'Oxigeno Industrial', 'ACTIVO'),
(3, 95.00, 'm3', 'NITROGENO', 'Nitrogeno Industrial', 'ACTIVO'),
(4, 110.00, 'm3', 'ARGON', 'Argon Industrial', 'ACTIVO'),
(5, 130.00, 'm3', 'CO2', 'Dioxido de Carbono', 'ACTIVO');


-- =========================================================
-- 9. PEDIDO
-- =========================================================

INSERT INTO pedido
(id_pedido, fecha_entrega_estimada, fecha_registro,
 id_cliente, prioridad, estado)
VALUES
(1, '2026-09-22', '2026-09-18', 1, 'ALTA',  'RECIBIDO'),
(2, '2026-09-23', '2026-09-18', 2, 'MEDIA', 'EN_PREPARACION'),
(3, '2026-09-24', '2026-09-19', 3, 'ALTA',  'DESPACHADO'),
(4, '2026-09-25', '2026-09-19', 4, 'BAJA',  'ENTREGADO'),
(5, '2026-09-26', '2026-09-20', 5, 'MEDIA', 'RECIBIDO');


-- =========================================================
-- 10. DETALLE_PEDIDO
-- =========================================================

INSERT INTO detalle_pedido
(id_detalle, cantidad, id_pedido, id_producto, precio_unitario)
VALUES
(1, 10.00, 1, 1, 150.00),
(2, 15.00, 2, 2, 180.00),
(3, 20.00, 3, 3, 95.00),
(4, 12.00, 4, 4, 110.00),
(5, 18.00, 5, 5, 130.00);


-- =========================================================
-- 11. SEGUIMIENTO PEDIDO
-- =========================================================

INSERT INTO seguimientopedido
(id_seguimiento, id_pedido, fecha_hora, estado, observacion)
VALUES
(1, 1, '2026-09-18 08:30:00', 'RECIBIDO',
 'Pedido registrado correctamente.'),

(2, 2, '2026-09-18 10:15:00', 'EN_PREPARACION',
 'Pedido enviado al area de preparacion.'),

(3, 3, '2026-09-19 11:20:00', 'DESPACHADO',
 'Pedido enviado al area de distribucion.'),

(4, 4, '2026-09-20 13:45:00', 'ENTREGADO',
 'Entrega completada satisfactoriamente.'),

(5, 5, '2026-09-20 15:10:00', 'RECIBIDO',
 'Pedido recibido y pendiente de programacion.');


-- =========================================================
-- 12. ATENCION
-- =========================================================

INSERT INTO atencion
(id_atencion, id_cisterna, id_conductor, id_pedido, id_programador,
 fecha_fin, fecha_fin_programada, fecha_inicio,
 fecha_inicio_programada, estado, observaciones)
VALUES
(1, 1, 1, 1, 11,
 '2026-09-22 14:30:00',
 '2026-09-22 15:00:00',
 '2026-09-22 08:00:00',
 '2026-09-22 08:00:00',
 'EN PROCESO',
 'Atencion iniciada dentro del horario programado.'),

(2, 2, 2, 2, 12,
 NULL,
 '2026-09-23 16:00:00',
 NULL,
 '2026-09-23 09:00:00',
 'PROGRAMADA',
 'Atencion pendiente de inicio.'),

(3, 3, 3, 3, 13,
 '2026-09-24 12:30:00',
 '2026-09-24 13:00:00',
 '2026-09-24 08:30:00',
 '2026-09-24 08:00:00',
 'FINALIZADA',
 'Atencion completada con retraso leve.'),

(4, 4, 4, 4, 14,
 '2026-09-20 17:00:00',
 '2026-09-20 17:00:00',
 '2026-09-20 14:00:00',
 '2026-09-20 14:00:00',
 'FINALIZADA',
 'Entrega realizada correctamente.'),

(5, 5, 5, 5, 15,
 NULL,
 '2026-09-26 15:00:00',
 NULL,
 '2026-09-26 08:00:00',
 'PROGRAMADA',
 'Pendiente de atencion.');


-- =========================================================
-- 13. ALERTA
-- =========================================================

INSERT INTO alerta
(id_alerta, id_atencion, fecha_hora, estado, tipo, mensaje)
VALUES
(1, 1, '2026-09-22 11:30:00', 'ACTIVA',
 'DEMORA', 'La atencion presenta una demora de 30 minutos.'),

(2, 2, '2026-09-23 15:00:00', 'ACTIVA',
 'PENDIENTE', 'La atencion aun no ha iniciado.'),

(3, 3, '2026-09-24 12:00:00', 'ATENDIDA',
 'RETRASO', 'Se detecto retraso en la entrega.'),

(4, 4, '2026-09-20 16:30:00', 'CERRADA',
 'SEGUIMIENTO', 'Entrega proxima a finalizar.'),

(5, 5, '2026-09-26 14:30:00', 'ACTIVA',
 'PROGRAMACION', 'La atencion se encuentra proxima a su horario.');


-- =========================================================
-- 14. FALLA
-- =========================================================

INSERT INTO falla
(id_falla, id_cisterna, id_conductor, fecha_hora, estado, descripcion)
VALUES
(1, 1, 1, '2026-09-10 09:20:00', 'REPORTADA',
 'Fuga detectada en una valvula de presion.'),

(2, 2, 2, '2026-09-11 14:10:00', 'EN REVISION',
 'Falla en el sistema hidraulico.'),

(3, 3, 3, '2026-09-12 08:45:00', 'REPARADA',
 'Problema en el sistema electrico.'),

(4, 4, 4, '2026-09-13 16:30:00', 'REPORTADA',
 'Desgaste de frenos detectado.'),

(5, 5, 5, '2026-09-14 10:15:00', 'EN REVISION',
 'Anomalia en el sistema de bombeo.');


-- =========================================================
-- 15. HISTORIAL_MANTENIMIENTO
-- =========================================================

INSERT INTO historial_mantenimiento
(id_historial, id_cisterna, id_falla, id_tecnico, fecha,
 costo, descripcion, observaciones, resultado)
VALUES
(1, 1, 1, 6, '2026-09-10 13:00:00',
 450.00, 'Cambio de valvula de presion.',
 'Se verificaron conexiones despues de la reparacion.',
 'Funcionamiento normal.'),

(2, 2, 2, 7, '2026-09-11 17:00:00',
 620.00, 'Reparacion del sistema hidraulico.',
 'Se reemplazaron componentes internos.',
 'Sistema operativo.'),

(3, 3, 3, 8, '2026-09-12 14:00:00',
 350.00, 'Reparacion del sistema electrico.',
 'Se cambio cableado deteriorado.',
 'Falla solucionada.'),

(4, 4, 4, 9, '2026-09-14 09:30:00',
 780.00, 'Mantenimiento del sistema de frenos.',
 'Se sustituyeron pastillas y se ajustaron frenos.',
 'Sistema seguro.'),

(5, 5, 5, 10, '2026-09-15 11:45:00',
 510.00, 'Revision del sistema de bombeo.',
 'Se realizaron pruebas de presion.',
 'Funcionamiento estable.');


-- =========================================================
-- 16. FACTURA
-- =========================================================

INSERT INTO factura
(id_factura, fecha_emision, id_pedido, moneda,
 tipo_comprobante, numero)
VALUES
(1, '2026-09-18', 1, 'PEN', 'FACTURA', 'F001-000001'),
(2, '2026-09-18', 2, 'PEN', 'FACTURA', 'F001-000002'),
(3, '2026-09-19', 3, 'PEN', 'FACTURA', 'F001-000003'),
(4, '2026-09-19', 4, 'PEN', 'FACTURA', 'F001-000004'),
(5, '2026-09-20', 5, 'PEN', 'FACTURA', 'F001-000005');


-- =========================================================
-- 17. GUIA_REMISION
-- =========================================================

INSERT INTO guia_remision
(id_guia, fecha_emision, fecha_inicio_traslado, id_pedido,
 numero, motivo_traslado, punto_llegada, punto_partida)
VALUES
(1, '2026-09-18', '2026-09-22', 1, 'T001-000001',
 'Entrega de oxigeno medicinal',
 'Hospital San Marcos S.A.C.',
 'Planta without Lima'),

(2, '2026-09-18', '2026-09-23', 2, 'T001-000002',
 'Entrega de oxigeno industrial',
 'Clinica Vida Salud S.A.C.',
 'Planta without Lima'),

(3, '2026-09-19', '2026-09-24', 3, 'T001-000003',
 'Entrega de nitrogeno industrial',
 'Industrias del Pacifico S.A.',
 'Planta without Callao'),

(4, '2026-09-19', '2026-09-20', 4, 'T001-000004',
 'Entrega de argon industrial',
 'Laboratorios Andinos S.A.C.',
 'Planta without Lima'),

(5, '2026-09-20', '2026-09-26', 5, 'T001-000005',
 'Entrega de dioxido de carbono',
 'Metalurgica Nacional S.A.',
 'Planta without Callao');


-- =========================================================
-- 18. TOKEN
-- =========================================================

INSERT INTO token
(id, user_id, date_creation, date_expiration,
 access_type, token, type)
VALUES
(1, 1, '2026-09-20 08:00:00', '2026-09-27 08:00:00',
 'BEARER', 'test_access_token_001', 'TOKEN'),

(2, 2, '2026-09-20 09:00:00', '2026-09-27 09:00:00',
 'BEARER', 'test_access_token_002', 'TOKEN'),

(3, 3, '2026-09-20 10:00:00', '2026-10-20 10:00:00',
 'BEARER', 'test_refresh_token_003', 'REFRESH_TOKEN'),

(4, 4, '2026-09-20 11:00:00', '2026-10-20 11:00:00',
 'BEARER', 'test_refresh_token_004', 'REFRESH_TOKEN'),

(5, 21, '2026-09-20 12:00:00', '2026-09-27 12:00:00',
 'BEARER', 'test_admin_token_005', 'TOKEN');


COMMIT;
