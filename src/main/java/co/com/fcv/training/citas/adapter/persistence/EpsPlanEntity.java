package co.com.fcv.training.citas.adapter.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "eps_plans")
class EpsPlanEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eps_id", nullable = false) EpsEntity eps;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "regime_id", nullable = false) InsuranceRegimeEntity regime;
    @Column(nullable = false, length = 50) String code;
    @Column(nullable = false, length = 150) String name;
    @Column(nullable = false) boolean active;
    protected EpsPlanEntity() {}
}
