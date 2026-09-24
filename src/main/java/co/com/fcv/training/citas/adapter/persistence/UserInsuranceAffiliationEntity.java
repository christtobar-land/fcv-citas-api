package co.com.fcv.training.citas.adapter.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "user_insurance_affiliations")
class UserInsuranceAffiliationEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false) UserEntity user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false) EpsPlanEntity plan;
    @Column(name = "membership_number", length = 80) String membershipNumber;
    @Column(name = "is_current", nullable = false) boolean current;
    @Column(name = "valid_from") LocalDate validFrom;
    @Column(name = "valid_to") LocalDate validTo;
    protected UserInsuranceAffiliationEntity() {}
}
