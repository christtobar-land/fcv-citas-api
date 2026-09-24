CREATE TABLE IF NOT EXISTS specialties (
    id SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL UNIQUE,
    appointment_duration_minutes SMALLINT UNSIGNED NOT NULL,
    is_general BOOLEAN NOT NULL DEFAULT FALSE,
    requires_admin_approval BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_specialty_duration CHECK (appointment_duration_minutes IN (30, 60))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS professionals (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL UNIQUE,
    professional_code VARCHAR(40) NOT NULL UNIQUE,
    license_number VARCHAR(80) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_professionals_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS professional_specialties (
    professional_id BIGINT UNSIGNED NOT NULL,
    specialty_id SMALLINT UNSIGNED NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (professional_id, specialty_id),
    CONSTRAINT fk_prof_specialty_professional FOREIGN KEY (professional_id) REFERENCES professionals(id) ON DELETE CASCADE,
    CONSTRAINT fk_prof_specialty_specialty FOREIGN KEY (specialty_id) REFERENCES specialties(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS professional_locations (
    professional_id BIGINT UNSIGNED NOT NULL,
    location_id SMALLINT UNSIGNED NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (professional_id, location_id),
    CONSTRAINT fk_prof_location_professional FOREIGN KEY (professional_id) REFERENCES professionals(id) ON DELETE CASCADE,
    CONSTRAINT fk_prof_location_location FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

INSERT INTO specialties (id, code, name, appointment_duration_minutes, is_general, requires_admin_approval, active) VALUES
    (1, 'MEDICINA_GENERAL', 'Medicina General', 30, TRUE, FALSE, TRUE),
    (2, 'CARDIOLOGIA_ADULTO', 'Cardiología Adulto', 30, FALSE, TRUE, TRUE),
    (3, 'CARDIOLOGIA_PEDIATRICA', 'Cardiología Pediátrica', 30, FALSE, TRUE, TRUE),
    (4, 'MEDICINA_INTERNA', 'Medicina Interna', 30, FALSE, TRUE, TRUE),
    (5, 'PEDIATRIA', 'Pediatría', 30, FALSE, TRUE, TRUE),
    (6, 'NEFROLOGIA', 'Nefrología', 30, FALSE, TRUE, TRUE),
    (7, 'UROLOGIA', 'Urología', 30, FALSE, TRUE, TRUE),
    (8, 'GASTROENTEROLOGIA', 'Gastroenterología', 30, FALSE, TRUE, TRUE),
    (9, 'NEUMOLOGIA_ADULTO', 'Neumología Adulto', 30, FALSE, TRUE, TRUE),
    (10, 'ENDOCRINOLOGIA', 'Endocrinología', 30, FALSE, TRUE, TRUE),
    (11, 'ORTOPEDIA_TRAUMATOLOGIA', 'Ortopedia y Traumatología', 60, FALSE, TRUE, TRUE),
    (12, 'NEUROLOGIA', 'Neurología', 60, FALSE, TRUE, TRUE)
ON DUPLICATE KEY UPDATE
    name = VALUES(name), appointment_duration_minutes = VALUES(appointment_duration_minutes),
    is_general = VALUES(is_general), requires_admin_approval = VALUES(requires_admin_approval), active = VALUES(active);
