package co.com.fcv.training.citas.application;

import java.util.List;

public class SpecialtyService {
    public record Create(String code, String name, short durationMinutes, boolean general,
                         boolean requiresAdminApproval) {}
    public record Patch(String name, Short durationMinutes, Boolean general,
                        Boolean requiresAdminApproval, Boolean active) {}

    private final Ports.Offer offer;
    public SpecialtyService(Ports.Offer offer) { this.offer = offer; }
    public List<Ports.SpecialtyView> all() { return offer.specialties(); }
    public Ports.SpecialtyView create(Create command) {
        return offer.createSpecialty(command.code(), command.name(), command.durationMinutes(),
                command.general(), command.requiresAdminApproval());
    }
    public Ports.SpecialtyView patch(Short id, Patch command) {
        return offer.patchSpecialty(id, command.name(), command.durationMinutes(), command.general(),
                command.requiresAdminApproval(), command.active());
    }
}
