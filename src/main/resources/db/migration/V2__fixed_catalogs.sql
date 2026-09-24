CREATE TABLE IF NOT EXISTS appointment_statuses (
    id SMALLINT UNSIGNED PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    is_terminal BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reschedule_request_statuses (
    id SMALLINT UNSIGNED PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    is_terminal BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS insurance_regimes (
    id SMALLINT UNSIGNED PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS locations (
    id SMALLINT UNSIGNED PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(180) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

INSERT INTO appointment_statuses (id, code, name, is_terminal) VALUES
(1, 'REQUESTED', 'Solicitada / pendiente de aprobación', FALSE),
(2, 'APPROVED', 'Aprobada', FALSE),
(3, 'REJECTED', 'Rechazada', TRUE),
(4, 'CANCELLED', 'Cancelada', TRUE),
(5, 'COMPLETED', 'Atendida / completada', TRUE),
(6, 'NO_SHOW', 'No asistió', TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name), is_terminal = VALUES(is_terminal);

INSERT INTO reschedule_request_statuses (id, code, name, is_terminal) VALUES
(1, 'PENDING', 'Pendiente', FALSE),
(2, 'APPROVED', 'Aprobada', TRUE),
(3, 'REJECTED', 'Rechazada', TRUE),
(4, 'CANCELLED', 'Cancelada por el usuario', TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name), is_terminal = VALUES(is_terminal);

INSERT INTO insurance_regimes (id, code, name) VALUES
(1, 'CONTRIBUTIVO', 'Contributivo'),
(2, 'SUBSIDIADO', 'Subsidiado'),
(3, 'ESPECIAL', 'Especial'),
(4, 'EXCEPCION', 'Excepción'),
(5, 'PARTICULAR', 'Particular')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO locations (id, code, name, address, city, department, active) VALUES
(1, 'HIC', 'Hospital Internacional de Colombia (HIC)', 'Km 7 Autopista Bucaramanga - Piedecuesta, Valle de Menzulí', 'Piedecuesta', 'Santander', TRUE),
(2, 'ICV', 'Fundación Cardiovascular de Colombia - Instituto Cardiovascular (ICV)', 'Calle 155A No. 23-58, Urbanización El Bosque', 'Floridablanca', 'Santander', TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name), address = VALUES(address), city = VALUES(city), department = VALUES(department), active = VALUES(active);
