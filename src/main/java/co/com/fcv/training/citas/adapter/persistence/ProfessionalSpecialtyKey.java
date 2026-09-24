package co.com.fcv.training.citas.adapter.persistence;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
class ProfessionalSpecialtyKey implements Serializable {
    Long professionalId;
    Short specialtyId;
    protected ProfessionalSpecialtyKey() {}
    ProfessionalSpecialtyKey(Long professionalId, Short specialtyId) {
        this.professionalId = professionalId;
        this.specialtyId = specialtyId;
    }
    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ProfessionalSpecialtyKey key)) return false;
        return java.util.Objects.equals(professionalId, key.professionalId)
                && java.util.Objects.equals(specialtyId, key.specialtyId);
    }
    @Override public int hashCode() { return java.util.Objects.hash(professionalId, specialtyId); }
}
