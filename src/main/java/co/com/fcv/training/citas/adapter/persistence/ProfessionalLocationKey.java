package co.com.fcv.training.citas.adapter.persistence;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
class ProfessionalLocationKey implements Serializable {
    Long professionalId;
    Short locationId;
    protected ProfessionalLocationKey() {}
    ProfessionalLocationKey(Long professionalId, Short locationId) {
        this.professionalId = professionalId;
        this.locationId = locationId;
    }
    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ProfessionalLocationKey key)) return false;
        return java.util.Objects.equals(professionalId, key.professionalId)
                && java.util.Objects.equals(locationId, key.locationId);
    }
    @Override public int hashCode() { return java.util.Objects.hash(professionalId, locationId); }
}
