-- ============================================================
-- MEDIHEALTH PLUS · DATOS SEMILLA PARA PRUEBAS (idempotente)
-- Requiere que Flyway ya haya creado el esquema y los catálogos
-- (sedes, especialidades, EPS, roles) al arrancar citas-api.
--
-- Crea:
--   * 1 administrador    -> admin1@medih.com
--   * 24 médicos         -> medico1@medih.com ... medico24@medih.com
--       medico1..12  = sede 1 (1 por cada una de las 12 especialidades)
--       medico13..24 = sede 2 (1 por cada una de las 12 especialidades)
--     Cada médico pertenece a UNA sola sede y UNA sola especialidad.
--   * Disponibilidad y slots de 30 min hasta el 31-oct-2026
--     (lunes a sábado: 07:00-12:00 y 14:00-17:00).
--
-- Contraseña de todas las cuentas semilla: medih123
-- Los pacientes NO se crean aquí: se registran desde la UI (/register).
-- Re-ejecutar el script renueva la disponibilidad sin duplicar datos.
-- ============================================================

SET NAMES utf8mb4;

-- Hash BCrypt de "medih123"
SET @pwd := '$2a$10$xCBkswKVT3O9cJ8fYCh.yuOA.xBVKIT/3N4QjkcRQ0/pivLgwTZ5y';

-- ---------- 1. Administradores ----------
INSERT INTO users (first_name, last_name, document_type, document_number, email, phone, password_hash, active, email_verified) VALUES
('Marcela',  'Ortiz Duarte',     'CC', '1090000001', 'admin1@medih.com', '3150000001', @pwd, TRUE, TRUE)
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), active = TRUE;

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code = 'ADMIN'
WHERE u.email = 'admin1@medih.com';

-- ---------- 2. Médicos (n, nombre, apellidos, especialidad, sede) ----------
DROP TEMPORARY TABLE IF EXISTS seed_doctors;
CREATE TEMPORARY TABLE seed_doctors (
    n INT PRIMARY KEY,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    specialty_id SMALLINT UNSIGNED NOT NULL,
    location_id SMALLINT UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO seed_doctors (n, first_name, last_name, specialty_id, location_id) VALUES
-- Sede 1 · El Bosque (Floridablanca)
(1,  'Alejandro',     'Morales Ruiz',          1, 1),  -- Medicina General
(2,  'Sofía',         'Valenzuela Prieto',     2, 1),  -- Cardiología Adulto
(3,  'Camila',        'Restrepo Londoño',      3, 1),  -- Cardiología Pediátrica
(4,  'Andrés Felipe', 'Cardona Vélez',         4, 1),  -- Medicina Interna
(5,  'Natalia',       'Herrera Quintero',      5, 1),  -- Pediatría
(6,  'Julián',        'Ospina Barrera',        6, 1),  -- Nefrología
(7,  'Ricardo',       'Salazar Pineda',        7, 1),  -- Urología
(8,  'Valentina',     'Arango Mejía',          8, 1),  -- Gastroenterología
(9,  'Héctor Fabián', 'Duarte Mantilla',       9, 1),  -- Neumología Adulto
(10, 'Paula Andrea',  'Caicedo Rueda',        10, 1),  -- Endocrinología
(11, 'Sebastián',     'Cifuentes Navarro',    11, 1),  -- Ortopedia y Traumatología
(12, 'Mariana',       'Echeverri Gil',        12, 1),  -- Neurología
-- Sede 2 · Norte (Piedecuesta)
(13, 'Carolina',      'Gómez Acevedo',         1, 2),  -- Medicina General
(14, 'Mauricio',      'Beltrán Cortés',        2, 2),  -- Cardiología Adulto
(15, 'Daniela',       'Patiño Roldán',         3, 2),  -- Cardiología Pediátrica
(16, 'Gustavo Adolfo','Parra Lozano',          4, 2),  -- Medicina Interna
(17, 'Laura',         'Mendoza Castillo',      5, 2),  -- Pediatría
(18, 'Esteban',       'Rincón Agudelo',        6, 2),  -- Nefrología
(19, 'Felipe',        'Rojas Murillo',         7, 2),  -- Urología
(20, 'Isabella',      'Zapata Giraldo',        8, 2),  -- Gastroenterología
(21, 'Camilo',        'Torres Alvarado',       9, 2),  -- Neumología Adulto
(22, 'Lucía Fernanda','Montoya Peña',         10, 2),  -- Endocrinología
(23, 'Mateo',         'García Santamaría',    11, 2),  -- Ortopedia y Traumatología
(24, 'Juliana',       'Bermúdez Tovar',       12, 2);  -- Neurología

INSERT INTO users (first_name, last_name, document_type, document_number, email, phone, password_hash, active, email_verified)
SELECT d.first_name, d.last_name, 'CC', CAST(1100000000 + d.n AS CHAR),
       CONCAT('medico', d.n, '@medih.com'), CONCAT('3160000', LPAD(d.n, 3, '0')), @pwd, TRUE, TRUE
FROM seed_doctors d
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name), active = TRUE;

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM seed_doctors d
JOIN users u ON u.email = CONCAT('medico', d.n, '@medih.com')
JOIN roles r ON r.code = 'PROFESSIONAL';

INSERT INTO professionals (user_id, professional_code, license_number, active)
SELECT u.id, CONCAT('MED-', LPAD(d.n, 3, '0')), CONCAT('RM-', 20000 + d.n), TRUE
FROM seed_doctors d
JOIN users u ON u.email = CONCAT('medico', d.n, '@medih.com')
ON DUPLICATE KEY UPDATE active = TRUE;

INSERT IGNORE INTO professional_specialties (professional_id, specialty_id, is_primary, active)
SELECT p.id, d.specialty_id, TRUE, TRUE
FROM seed_doctors d
JOIN users u ON u.email = CONCAT('medico', d.n, '@medih.com')
JOIN professionals p ON p.user_id = u.id;

INSERT IGNORE INTO professional_locations (professional_id, location_id, active)
SELECT p.id, d.location_id, TRUE
FROM seed_doctors d
JOIN users u ON u.email = CONCAT('medico', d.n, '@medih.com')
JOIN professionals p ON p.user_id = u.id;

-- ---------- 3. Disponibilidad: desde mañana hasta el 31-oct-2026, lunes a sábado ----------
SET @end_date := '2026-10-31';
DROP TEMPORARY TABLE IF EXISTS seed_days;
CREATE TEMPORARY TABLE seed_days (day_offset INT PRIMARY KEY);
INSERT INTO seed_days (day_offset) VALUES
(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12),(13),(14),(15),(16),(17),(18),(19),(20),
(21),(22),(23),(24),(25),(26),(27),(28),(29),(30),(31);

DROP TEMPORARY TABLE IF EXISTS seed_shifts;
CREATE TEMPORARY TABLE seed_shifts (start_time TIME PRIMARY KEY, end_time TIME NOT NULL);
INSERT INTO seed_shifts (start_time, end_time) VALUES ('07:00:00', '12:00:00'), ('14:00:00', '17:00:00');

INSERT INTO availability_blocks (professional_id, location_id, available_date, start_time, end_time, active)
SELECT p.id, d.location_id, DATE_ADD(CURDATE(), INTERVAL sd.day_offset DAY), sh.start_time, sh.end_time, TRUE
FROM seed_doctors d
JOIN users u ON u.email = CONCAT('medico', d.n, '@medih.com')
JOIN professionals p ON p.user_id = u.id
CROSS JOIN seed_days sd
CROSS JOIN seed_shifts sh
WHERE DATE_ADD(CURDATE(), INTERVAL sd.day_offset DAY) <= @end_date
  AND DAYOFWEEK(DATE_ADD(CURDATE(), INTERVAL sd.day_offset DAY)) <> 1   -- sin domingos
  AND NOT EXISTS (
      SELECT 1 FROM availability_blocks ab
      WHERE ab.professional_id = p.id
        AND ab.location_id = d.location_id
        AND ab.available_date = DATE_ADD(CURDATE(), INTERVAL sd.day_offset DAY)
        AND ab.start_time = sh.start_time
  );

-- ---------- 4. Slots atómicos de 30 minutos ----------
INSERT IGNORE INTO professional_slots (availability_block_id, start_at, end_at)
SELECT ab.id,
       TIMESTAMP(ab.available_date, ADDTIME(ab.start_time, SEC_TO_TIME(seq.n * 1800))),
       TIMESTAMP(ab.available_date, ADDTIME(ab.start_time, SEC_TO_TIME((seq.n + 1) * 1800)))
FROM availability_blocks ab
JOIN (
    SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3
    UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7
    UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11
) seq
WHERE ab.active = TRUE
  AND ab.available_date >= CURDATE()
  AND ADDTIME(ab.start_time, SEC_TO_TIME((seq.n + 1) * 1800)) <= ab.end_time;

DROP TEMPORARY TABLE IF EXISTS seed_doctors;
DROP TEMPORARY TABLE IF EXISTS seed_days;
DROP TEMPORARY TABLE IF EXISTS seed_shifts;

-- ---------- 5. Resumen ----------
SELECT 'Administradores' AS recurso, COUNT(*) AS total FROM user_roles ur JOIN roles r ON r.id = ur.role_id AND r.code = 'ADMIN'
UNION ALL SELECT 'Médicos', COUNT(*) FROM professionals
UNION ALL SELECT 'Especialidades con médico en sede 1', COUNT(DISTINCT ps.specialty_id) FROM professional_specialties ps JOIN professional_locations pl ON pl.professional_id = ps.professional_id AND pl.location_id = 1
UNION ALL SELECT 'Especialidades con médico en sede 2', COUNT(DISTINCT ps.specialty_id) FROM professional_specialties ps JOIN professional_locations pl ON pl.professional_id = ps.professional_id AND pl.location_id = 2
UNION ALL SELECT 'Bloques de disponibilidad', COUNT(*) FROM availability_blocks
UNION ALL SELECT 'Slots disponibles', COUNT(*) FROM professional_slots WHERE appointment_id IS NULL AND start_at >= NOW();
