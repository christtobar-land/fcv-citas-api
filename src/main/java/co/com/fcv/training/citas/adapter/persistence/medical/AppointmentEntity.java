package co.com.fcv.training.citas.adapter.persistence.medical;

import co.com.fcv.training.citas.adapter.persistence.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class AppointmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_user_id", nullable = false)
    private UserEntity patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "professional_id", nullable = false)
    private ProfessionalEntity professional;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id", nullable = false)
    private LocationEntity location;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "specialty_id", nullable = false)
    private SpecialtyEntity specialty;

    @Column(name = "insurance_affiliation_id")
    private Long insuranceAffiliationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id", nullable = false)
    private AppointmentStatusEntity status;

    @Column(length = 500)
    private String reason;

    @Column(name = "referral_code", length = 80)
    private String referralCode;

    @Column(name = "scheduled_start_at", nullable = false)
    private LocalDateTime scheduledStartAt;

    @Column(name = "scheduled_end_at", nullable = false)
    private LocalDateTime scheduledEndAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private UserEntity createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private UserEntity approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UserEntity getPatient() { return patient; }
    public void setPatient(UserEntity patient) { this.patient = patient; }
    public ProfessionalEntity getProfessional() { return professional; }
    public void setProfessional(ProfessionalEntity professional) { this.professional = professional; }
    public LocationEntity getLocation() { return location; }
    public void setLocation(LocationEntity location) { this.location = location; }
    public SpecialtyEntity getSpecialty() { return specialty; }
    public void setSpecialty(SpecialtyEntity specialty) { this.specialty = specialty; }
    public Long getInsuranceAffiliationId() { return insuranceAffiliationId; }
    public void setInsuranceAffiliationId(Long insuranceAffiliationId) { this.insuranceAffiliationId = insuranceAffiliationId; }
    public AppointmentStatusEntity getStatus() { return status; }
    public void setStatus(AppointmentStatusEntity status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getReferralCode() { return referralCode; }
    public void setReferralCode(String referralCode) { this.referralCode = referralCode; }
    public LocalDateTime getScheduledStartAt() { return scheduledStartAt; }
    public void setScheduledStartAt(LocalDateTime scheduledStartAt) { this.scheduledStartAt = scheduledStartAt; }
    public LocalDateTime getScheduledEndAt() { return scheduledEndAt; }
    public void setScheduledEndAt(LocalDateTime scheduledEndAt) { this.scheduledEndAt = scheduledEndAt; }
    public UserEntity getCreatedBy() { return createdBy; }
    public void setCreatedBy(UserEntity createdBy) { this.createdBy = createdBy; }
    public UserEntity getApprovedBy() { return approvedBy; }
    public void setApprovedBy(UserEntity approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
