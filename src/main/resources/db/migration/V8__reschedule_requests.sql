CREATE TABLE IF NOT EXISTS reschedule_requests (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT UNSIGNED NOT NULL,
    requested_location_id SMALLINT UNSIGNED NOT NULL,
    requested_start_at DATETIME NOT NULL,
    requested_end_at DATETIME NOT NULL,
    status_id SMALLINT UNSIGNED NOT NULL,
    requested_by_user_id BIGINT UNSIGNED NOT NULL,
    decided_by_user_id BIGINT UNSIGNED NULL,
    decision_reason VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    decided_at DATETIME NULL,
    CONSTRAINT ck_reschedule_time CHECK (requested_end_at > requested_start_at),
    CONSTRAINT fk_reschedule_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE RESTRICT,
    CONSTRAINT fk_reschedule_location FOREIGN KEY (requested_location_id) REFERENCES locations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_reschedule_status FOREIGN KEY (status_id) REFERENCES reschedule_request_statuses(id) ON DELETE RESTRICT,
    CONSTRAINT fk_reschedule_requester FOREIGN KEY (requested_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_reschedule_decider FOREIGN KEY (decided_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX ix_reschedule_appointment (appointment_id),
    INDEX ix_reschedule_status (status_id)
) ENGINE=InnoDB;

ALTER TABLE professional_slots
    ADD COLUMN reschedule_request_id BIGINT UNSIGNED NULL,
    ADD CONSTRAINT fk_slots_reschedule_request FOREIGN KEY (reschedule_request_id) REFERENCES reschedule_requests(id) ON DELETE SET NULL,
    ADD INDEX ix_slots_reschedule_request (reschedule_request_id);
