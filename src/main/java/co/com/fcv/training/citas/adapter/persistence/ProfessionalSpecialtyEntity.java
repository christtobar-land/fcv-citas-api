package co.com.fcv.training.citas.adapter.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "professional_specialties")
class ProfessionalSpecialtyEntity {
    @EmbeddedId ProfessionalSpecialtyKey id;
    @Column(name = "is_primary", nullable = false) boolean primarySpecialty;
    @Column(nullable = false) boolean active;
    protected ProfessionalSpecialtyEntity() {}
    ProfessionalSpecialtyEntity(Long professionalId, Short specialtyId, boolean primarySpecialty) {
        this.id = new ProfessionalSpecialtyKey(professionalId, specialtyId);
        this.primarySpecialty = primarySpecialty;
        this.active = true;
    }
}
