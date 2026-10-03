package co.com.fcv.training.citas.adapter.persistence.medical;

import jakarta.persistence.*;

@Entity
@Table(name = "eps_plans")
public class EpsPlanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eps_id", nullable = false)
    private EpsEntity eps;

    @Column(name = "regime_id", nullable = false)
    private Short regimeId;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private Boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public EpsEntity getEps() { return eps; }
    public void setEps(EpsEntity eps) { this.eps = eps; }
    public Short getRegimeId() { return regimeId; }
    public void setRegimeId(Short regimeId) { this.regimeId = regimeId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
