CREATE TABLE IF NOT EXISTS availability_blocks (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    professional_id BIGINT UNSIGNED NOT NULL,
    location_id SMALLINT UNSIGNED NOT NULL,
    available_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT ck_availability_block_time CHECK (end_time > start_time),
    CONSTRAINT fk_availability_professional FOREIGN KEY (professional_id) REFERENCES professionals(id) ON DELETE RESTRICT,
    CONSTRAINT fk_availability_location FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE RESTRICT,
    INDEX ix_availability_prof_date (professional_id, available_date, start_time),
    INDEX ix_availability_location_date (location_id, available_date)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS appointments (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    patient_user_id BIGINT UNSIGNED NOT NULL,
    professional_id BIGINT UNSIGNED NOT NULL,
    location_id SMALLINT UNSIGNED NOT NULL,
    specialty_id SMALLINT UNSIGNED NOT NULL,
    insurance_affiliation_id BIGINT UNSIGNED NULL,
    status_id SMALLINT UNSIGNED NOT NULL,
    reason VARCHAR(500) NULL,
    scheduled_start_at DATETIME NOT NULL,
    scheduled_end_at DATETIME NOT NULL,
    created_by_user_id BIGINT UNSIGNED NOT NULL,
    approved_by_user_id BIGINT UNSIGNED NULL,
    approved_at DATETIME NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT ck_appointment_time CHECK (scheduled_end_at > scheduled_start_at),
    CONSTRAINT fk_appointments_patient FOREIGN KEY (patient_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_appointments_professional FOREIGN KEY (professional_id) REFERENCES professionals(id) ON DELETE RESTRICT,
    CONSTRAINT fk_appointments_location FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_appointments_specialty FOREIGN KEY (specialty_id) REFERENCES specialties(id) ON DELETE RESTRICT,
    CONSTRAINT fk_appointments_insurance FOREIGN KEY (insurance_affiliation_id) REFERENCES user_insurance_affiliations(id) ON DELETE SET NULL,
    CONSTRAINT fk_appointments_status FOREIGN KEY (status_id) REFERENCES appointment_statuses(id) ON DELETE RESTRICT,
    CONSTRAINT fk_appointments_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_appointments_approved_by FOREIGN KEY (approved_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX ix_appointments_patient (patient_user_id, scheduled_start_at),
    INDEX ix_appointments_professional (professional_id, scheduled_start_at),
    INDEX ix_appointments_status (status_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS professional_slots (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    availability_block_id BIGINT UNSIGNED NOT NULL,
    start_at DATETIME NOT NULL,
    end_at DATETIME NOT NULL,
    appointment_id BIGINT UNSIGNED NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_professional_slot_time CHECK (end_at > start_at),
    CONSTRAINT uq_block_slot UNIQUE (availability_block_id, start_at),
    CONSTRAINT fk_slots_availability_block FOREIGN KEY (availability_block_id) REFERENCES availability_blocks(id) ON DELETE RESTRICT,
    CONSTRAINT fk_slots_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE SET NULL,
    INDEX ix_slots_start (start_at),
    INDEX ix_slots_appointment (appointment_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS appointment_status_history (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT UNSIGNED NOT NULL,
    status_id SMALLINT UNSIGNED NOT NULL,
    changed_by_user_id BIGINT UNSIGNED NULL,
    change_source VARCHAR(20) NOT NULL,
    reason VARCHAR(500) NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_status_history_source CHECK (change_source IN ('SYSTEM', 'USER', 'ADMIN')),
    CONSTRAINT fk_status_history_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    CONSTRAINT fk_status_history_status FOREIGN KEY (status_id) REFERENCES appointment_statuses(id) ON DELETE RESTRICT,
    CONSTRAINT fk_status_history_user FOREIGN KEY (changed_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX ix_status_history_appointment (appointment_id, changed_at)
) ENGINE=InnoDB;
