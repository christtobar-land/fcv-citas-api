package co.com.fcv.training.citas.adapter.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "professionals")
class ProfessionalEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @Column(name = "user_id", nullable = false, unique = true) Long userId;
    @Column(name = "professional_code", nullable = false, length = 40) String professionalCode;
    @Column(name = "license_number", nullable = false, length = 80) String licenseNumber;
    @Column(nullable = false) boolean active;
    protected ProfessionalEntity() {}
}
