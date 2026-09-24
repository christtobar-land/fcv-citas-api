package co.com.fcv.training.citas.adapter.persistence;

import co.com.fcv.training.citas.application.Ports;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;

@Repository
class OfferJpaAdapter implements Ports.Offer {
    private final SpecialtiesJpa specialties;
    private final ProfessionalsJpa professionals;
    private final ProfessionalSpecialtiesJpa professionalSpecialties;
    private final ProfessionalLocationsJpa professionalLocations;
    private final LocationsJpa locations;

    OfferJpaAdapter(SpecialtiesJpa specialties, ProfessionalsJpa professionals,
                    ProfessionalSpecialtiesJpa professionalSpecialties,
                    ProfessionalLocationsJpa professionalLocations, LocationsJpa locations) {
        this.specialties = specialties;
        this.professionals = professionals;
        this.professionalSpecialties = professionalSpecialties;
        this.professionalLocations = professionalLocations;
        this.locations = locations;
    }

    @Override @Transactional(readOnly = true)
    public List<Ports.SpecialtyView> specialties() {
        return specialties.findAllByOrderById().stream().map(this::specialty).toList();
    }

    @Override @Transactional
    public Ports.SpecialtyView createSpecialty(String code, String name, short durationMinutes,
                                               boolean general, boolean requiresAdminApproval) {
        validateDuration(durationMinutes);
        if (general && requiresAdminApproval) throw new IllegalArgumentException("Medicina general no requiere aprobación");
        SpecialtyEntity entity = new SpecialtyEntity();
        entity.code = code.trim(); entity.name = name.trim(); entity.appointmentDurationMinutes = durationMinutes;
        entity.general = general; entity.requiresAdminApproval = requiresAdminApproval; entity.active = true;
        return specialty(specialties.saveAndFlush(entity));
    }

    @Override @Transactional
    public Ports.SpecialtyView patchSpecialty(Short id, String name, Short durationMinutes, Boolean general,
                                              Boolean requiresAdminApproval, Boolean active) {
        SpecialtyEntity entity = specialties.findById(id).orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada"));
        short nextDuration = durationMinutes == null ? entity.appointmentDurationMinutes : durationMinutes;
        boolean nextGeneral = general == null ? entity.general : general;
        boolean nextApproval = requiresAdminApproval == null ? entity.requiresAdminApproval : requiresAdminApproval;
        validateDuration(nextDuration);
        if (nextGeneral && nextApproval) throw new IllegalArgumentException("Medicina general no requiere aprobación");
        if (name != null && !name.isBlank()) entity.name = name.trim();
        entity.appointmentDurationMinutes = nextDuration; entity.general = nextGeneral;
        entity.requiresAdminApproval = nextApproval;
        if (active != null) entity.active = active;
        return specialty(specialties.saveAndFlush(entity));
    }

    @Override @Transactional
    public Ports.ProfessionalView createProfessional(Long userId, String professionalCode, String licenseNumber) {
        ProfessionalEntity entity = new ProfessionalEntity();
        entity.userId = userId; entity.professionalCode = professionalCode.trim();
        entity.licenseNumber = licenseNumber.trim(); entity.active = true;
        return professional(professionals.saveAndFlush(entity));
    }

    @Override @Transactional
    public void replaceSpecialties(Long professionalId, List<Short> specialtyIds, Short primarySpecialtyId) {
        requireProfessional(professionalId);
        if (specialtyIds == null || specialtyIds.isEmpty() || primarySpecialtyId == null
                || !new HashSet<>(specialtyIds).contains(primarySpecialtyId)) {
            throw new IllegalArgumentException("Debe asignarse al menos una especialidad y una primaria");
        }
        List<SpecialtyEntity> found = specialties.findAllById(specialtyIds);
        if (found.size() != new HashSet<>(specialtyIds).size() || found.stream().anyMatch(s -> !s.active)) {
            throw new IllegalArgumentException("Todas las especialidades deben existir y estar activas");
        }
        professionalSpecialties.deleteByProfessionalId(professionalId);
        professionalSpecialties.saveAllAndFlush(specialtyIds.stream()
                .distinct().map(id -> new ProfessionalSpecialtyEntity(professionalId, id, id.equals(primarySpecialtyId))).toList());
    }

    @Override @Transactional
    public void replaceLocations(Long professionalId, List<Short> locationIds) {
        requireProfessional(professionalId);
        if (locationIds == null || locationIds.isEmpty() || locationIds.size() > 2
                || new HashSet<>(locationIds).size() != locationIds.size()) {
            throw new IllegalArgumentException("El profesional debe tener una o ambas sedes");
        }
        List<LocationEntity> found = locations.findAllById(locationIds);
        if (found.size() != locationIds.size() || found.stream().anyMatch(l -> !l.active)) {
            throw new IllegalArgumentException("Todas las sedes deben existir y estar activas");
        }
        professionalLocations.deleteByProfessionalId(professionalId);
        professionalLocations.saveAllAndFlush(locationIds.stream().map(id -> new ProfessionalLocationEntity(professionalId, id)).toList());
    }

    @Override @Transactional
    public void setProfessionalActive(Long professionalId, boolean active) {
        ProfessionalEntity entity = requireProfessional(professionalId);
        entity.active = active;
        professionals.saveAndFlush(entity);
    }

    private ProfessionalEntity requireProfessional(Long id) {
        return professionals.findById(id).orElseThrow(() -> new IllegalArgumentException("Profesional no encontrado"));
    }

    private void validateDuration(short duration) {
        if (duration != 30 && duration != 60) throw new IllegalArgumentException("La duración debe ser 30 o 60 minutos");
    }

    private Ports.SpecialtyView specialty(SpecialtyEntity e) {
        return new Ports.SpecialtyView(e.id, e.code, e.name, e.appointmentDurationMinutes, e.general,
                e.requiresAdminApproval, e.active);
    }

    private Ports.ProfessionalView professional(ProfessionalEntity e) {
        return new Ports.ProfessionalView(e.id, e.userId, e.professionalCode, e.licenseNumber, e.active);
    }
}
