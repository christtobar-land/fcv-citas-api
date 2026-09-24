package co.com.fcv.training.citas.adapter.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "specialties")
class SpecialtyEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Short id;
    @Column(nullable = false, length = 50) String code;
    @Column(nullable = false, length = 150) String name;
    @Column(name = "appointment_duration_minutes", nullable = false) short appointmentDurationMinutes;
    @Column(name = "is_general", nullable = false) boolean general;
    @Column(name = "requires_admin_approval", nullable = false) boolean requiresAdminApproval;
    @Column(nullable = false) boolean active;
    protected SpecialtyEntity() {}
}
