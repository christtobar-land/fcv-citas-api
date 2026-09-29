-- Agenda de laboratorio: al ejecutarse el 2026-09-29 cubre todo octubre.
-- Se generan los 31 días siguientes al primer día futuro y nunca se incluyen credenciales utilizables.
UPDATE users SET first_name = 'Laura', last_name = 'Mendez'
WHERE email = 'profesional.demo@laboratorio.invalid';

INSERT INTO users (first_name, last_name, document_type, document_number, email, password_hash, active, email_verified)
VALUES
  ('Martin', 'Salazar', 'DEMO', 'PROF-GEN-002', 'martin.salazar@laboratorio.invalid', '!synthetic-non-loginable-account!', FALSE, FALSE),
  ('Elena', 'Rios', 'DEMO', 'PROF-CAR-001', 'elena.rios@laboratorio.invalid', '!synthetic-non-loginable-account!', FALSE, FALSE),
  ('Sofia', 'Vega', 'DEMO', 'PROF-PED-001', 'sofia.vega@laboratorio.invalid', '!synthetic-non-loginable-account!', FALSE, FALSE),
  ('Andres', 'Luna', 'DEMO', 'PROF-ORT-001', 'andres.luna@laboratorio.invalid', '!synthetic-non-loginable-account!', FALSE, FALSE)
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name);

INSERT INTO professionals (user_id, professional_code, license_number, active)
SELECT u.id, x.professional_code, x.license_number, TRUE
FROM users u
JOIN (
  SELECT 'martin.salazar@laboratorio.invalid' AS email, 'DEMO-GEN-002' AS professional_code, 'LIC-DEMO-GEN-002' AS license_number
  UNION ALL SELECT 'elena.rios@laboratorio.invalid', 'DEMO-CAR-001', 'LIC-DEMO-CAR-001'
  UNION ALL SELECT 'sofia.vega@laboratorio.invalid', 'DEMO-PED-001', 'LIC-DEMO-PED-001'
  UNION ALL SELECT 'andres.luna@laboratorio.invalid', 'DEMO-ORT-001', 'LIC-DEMO-ORT-001'
) x ON x.email = u.email
ON DUPLICATE KEY UPDATE active = VALUES(active);

INSERT INTO professional_specialties (professional_id, specialty_id, is_primary, active)
SELECT p.id, x.specialty_id, TRUE, TRUE
FROM professionals p
JOIN (
  SELECT 'DEMO-GEN-002' AS professional_code, 1 AS specialty_id
  UNION ALL SELECT 'DEMO-CAR-001', 2
  UNION ALL SELECT 'DEMO-PED-001', 5
  UNION ALL SELECT 'DEMO-ORT-001', 11
) x ON x.professional_code = p.professional_code
ON DUPLICATE KEY UPDATE is_primary = VALUES(is_primary), active = VALUES(active);

INSERT INTO professional_locations (professional_id, location_id, active)
SELECT p.id, x.location_id, TRUE
FROM professionals p
JOIN (
  SELECT 'DEMO-GEN-002' AS professional_code, 1 AS location_id
  UNION ALL SELECT 'DEMO-CAR-001', 2
  UNION ALL SELECT 'DEMO-PED-001', 1
  UNION ALL SELECT 'DEMO-ORT-001', 2
) x ON x.professional_code = p.professional_code
ON DUPLICATE KEY UPDATE active = VALUES(active);

INSERT INTO availability_blocks (professional_id, location_id, available_date, start_time, end_time, active)
SELECT p.id, schedule.location_id, DATE_ADD(CURRENT_DATE, INTERVAL days_after.n DAY), schedule.start_time, schedule.end_time, TRUE
FROM professionals p
JOIN (
  SELECT 'DEMO-GEN-002' AS professional_code, 1 AS location_id, '08:00:00' AS start_time, '12:00:00' AS end_time
  UNION ALL SELECT 'DEMO-CAR-001', 2, '09:00:00', '13:00:00'
  UNION ALL SELECT 'DEMO-PED-001', 1, '13:00:00', '17:00:00'
  UNION ALL SELECT 'DEMO-ORT-001', 2, '14:00:00', '18:00:00'
) schedule ON schedule.professional_code = p.professional_code
JOIN (
  SELECT 2 AS n UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
  UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
  UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20 UNION ALL SELECT 21 UNION ALL SELECT 22
  UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25 UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29
  UNION ALL SELECT 30 UNION ALL SELECT 31 UNION ALL SELECT 32
) days_after;

INSERT INTO professional_slots (availability_block_id, start_at, end_at)
SELECT b.id, DATE_ADD(TIMESTAMP(b.available_date, b.start_time), INTERVAL offsets.n * 30 MINUTE),
       DATE_ADD(TIMESTAMP(b.available_date, b.start_time), INTERVAL (offsets.n + 1) * 30 MINUTE)
FROM availability_blocks b
JOIN professionals p ON p.id = b.professional_id
JOIN (
  SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3
  UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7
) offsets
WHERE p.professional_code IN ('DEMO-GEN-002', 'DEMO-CAR-001', 'DEMO-PED-001', 'DEMO-ORT-001')
  AND b.available_date BETWEEN DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY) AND DATE_ADD(CURRENT_DATE, INTERVAL 32 DAY);
