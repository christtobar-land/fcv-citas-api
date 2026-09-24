package co.com.fcv.training.citas.adapter.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "professional_locations")
class ProfessionalLocationEntity {
    @EmbeddedId ProfessionalLocationKey id;
    @Column(nullable = false) boolean active;
    protected ProfessionalLocationEntity() {}
    ProfessionalLocationEntity(Long professionalId, Short locationId) {
        this.id = new ProfessionalLocationKey(professionalId, locationId);
        this.active = true;
    }
}
