-- Datos estrictamente sintéticos para demostrar el flujo local de búsqueda y reserva.
-- La identidad técnica no tiene rol ni una contraseña utilizable y no permite iniciar sesión.
INSERT INTO users (first_name, last_name, document_type, document_number, email, phone, password_hash, active, email_verified)
SELECT 'Profesional', 'Demostracion', 'DEMO', 'PROF-AGENDA-001', 'profesional.demo@laboratorio.invalid', NULL,
       '!synthetic-non-loginable-account!', FALSE, FALSE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'profesional.demo@laboratorio.invalid');

INSERT INTO professionals (user_id, professional_code, license_number, active)
SELECT u.id, 'DEMO-AGENDA-001', 'LIC-DEMO-AGENDA-001', TRUE
FROM users u
WHERE u.email = 'profesional.demo@laboratorio.invalid'
  AND NOT EXISTS (SELECT 1 FROM professionals p WHERE p.professional_code = 'DEMO-AGENDA-001');

INSERT INTO professional_specialties (professional_id, specialty_id, is_primary, active)
SELECT p.id, s.id, s.id = 1, TRUE
FROM professionals p
JOIN specialties s ON s.id IN (1, 2)
WHERE p.professional_code = 'DEMO-AGENDA-001'
ON DUPLICATE KEY UPDATE is_primary = VALUES(is_primary), active = VALUES(active);

INSERT INTO professional_locations (professional_id, location_id, active)
SELECT p.id, l.id, TRUE
FROM professionals p
JOIN locations l ON l.id IN (1, 2)
WHERE p.professional_code = 'DEMO-AGENDA-001'
ON DUPLICATE KEY UPDATE active = VALUES(active);

INSERT INTO availability_blocks (professional_id, location_id, available_date, start_time, end_time, active)
SELECT p.id, d.location_id, d.available_date, '08:00:00', '12:00:00', TRUE
FROM professionals p
JOIN (
    SELECT 1 AS location_id, CURRENT_DATE + INTERVAL 2 DAY AS available_date
    UNION ALL SELECT 1, CURRENT_DATE + INTERVAL 4 DAY
    UNION ALL SELECT 2, CURRENT_DATE + INTERVAL 3 DAY
) d
WHERE p.professional_code = 'DEMO-AGENDA-001';

INSERT INTO professional_slots (availability_block_id, start_at, end_at)
SELECT b.id, TIMESTAMP(b.available_date, t.start_time), TIMESTAMP(b.available_date, t.end_time)
FROM availability_blocks b
JOIN professionals p ON p.id = b.professional_id AND p.professional_code = 'DEMO-AGENDA-001'
JOIN (
    SELECT '08:00:00' AS start_time, '08:30:00' AS end_time
    UNION ALL SELECT '08:30:00', '09:00:00'
    UNION ALL SELECT '09:00:00', '09:30:00'
    UNION ALL SELECT '09:30:00', '10:00:00'
    UNION ALL SELECT '10:00:00', '10:30:00'
    UNION ALL SELECT '10:30:00', '11:00:00'
    UNION ALL SELECT '11:00:00', '11:30:00'
    UNION ALL SELECT '11:30:00', '12:00:00'
) t
WHERE b.available_date IN (CURRENT_DATE + INTERVAL 2 DAY, CURRENT_DATE + INTERVAL 3 DAY, CURRENT_DATE + INTERVAL 4 DAY);
