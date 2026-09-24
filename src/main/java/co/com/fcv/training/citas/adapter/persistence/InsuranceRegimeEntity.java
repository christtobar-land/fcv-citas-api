package co.com.fcv.training.citas.adapter.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "insurance_regimes")
class InsuranceRegimeEntity {
    @Id Short id;
    @Column(nullable = false, length = 30) String code;
    @Column(nullable = false, length = 80) String name;
    protected InsuranceRegimeEntity() {}
}
