package co.com.fcv.training.citas.adapter.persistence.medical;

import jakarta.persistence.*;

@Entity
@Table(name = "specialties")
public class SpecialtyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "appointment_duration_minutes", nullable = false)
    private Short appointmentDurationMinutes;

    @Column(name = "is_general", nullable = false)
    private Boolean isGeneral = false;

    @Column(name = "requires_admin_approval", nullable = false)
    private Boolean requiresAdminApproval = true;

    @Column(nullable = false)
    private Boolean active = true;

    public Short getId() { return id; }
    public void setId(Short id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Short getAppointmentDurationMinutes() { return appointmentDurationMinutes; }
    public void setAppointmentDurationMinutes(Short appointmentDurationMinutes) { this.appointmentDurationMinutes = appointmentDurationMinutes; }
    public Boolean getIsGeneral() { return isGeneral; }
    public void setIsGeneral(Boolean isGeneral) { this.isGeneral = isGeneral; }
    public Boolean getRequiresAdminApproval() { return requiresAdminApproval; }
    public void setRequiresAdminApproval(Boolean requiresAdminApproval) { this.requiresAdminApproval = requiresAdminApproval; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
