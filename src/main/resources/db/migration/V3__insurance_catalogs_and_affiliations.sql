CREATE TABLE IF NOT EXISTS eps (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS eps_plans (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    eps_id BIGINT UNSIGNED NOT NULL,
    regime_id SMALLINT UNSIGNED NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_eps_plan_code UNIQUE (eps_id, code),
    CONSTRAINT fk_eps_plans_eps FOREIGN KEY (eps_id) REFERENCES eps(id) ON DELETE RESTRICT,
    CONSTRAINT fk_eps_plans_regime FOREIGN KEY (regime_id) REFERENCES insurance_regimes(id) ON DELETE RESTRICT,
    INDEX ix_eps_plans_active (active),
    INDEX ix_eps_plans_regime (regime_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS user_insurance_affiliations (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    plan_id BIGINT UNSIGNED NOT NULL,
    membership_number VARCHAR(80) NULL,
    is_current BOOLEAN NOT NULL DEFAULT TRUE,
    valid_from DATE NULL,
    valid_to DATE NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_membership UNIQUE (user_id, plan_id, membership_number),
    CONSTRAINT fk_user_insurance_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_insurance_plan FOREIGN KEY (plan_id) REFERENCES eps_plans(id) ON DELETE RESTRICT,
    INDEX ix_user_insurance_current (user_id, is_current)
) ENGINE=InnoDB;

INSERT INTO eps (id, code, name, active) VALUES
    (1, 'EPS_DEMO_A', 'EPS Demo Salud', TRUE),
    (2, 'EPS_DEMO_B', 'EPS Demo Familiar', TRUE),
    (3, 'PARTICULAR_DEMO', 'Atención Particular Demo', TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name), active = VALUES(active);

INSERT INTO eps_plans (id, eps_id, regime_id, code, name, active) VALUES
    (1, 1, 1, 'A-CONTRIB', 'Plan Contributivo Demo', TRUE),
    (2, 1, 2, 'A-SUBS', 'Plan Subsidiado Demo', TRUE),
    (3, 2, 1, 'B-CONTRIB', 'Plan Contributivo Familiar Demo', TRUE),
    (4, 2, 3, 'B-ESPECIAL', 'Plan Especial Demo', TRUE),
    (5, 3, 5, 'PARTICULAR', 'Particular / pago directo', TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name), active = VALUES(active), regime_id = VALUES(regime_id);
