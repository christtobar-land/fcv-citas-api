package co.com.fcv.training.citas.adapter.persistence;

import co.com.fcv.training.citas.application.Ports;
import co.com.fcv.training.citas.application.SchedulingConflict;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
class SchedulingJpaAdapter implements Ports.Scheduling {
    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final int SLOT_MINUTES = 30;
    private final ProfessionalsJpa professionals;
    private final ProfessionalLocationsJpa professionalLocations;
    private final SpecialtiesJpa specialties;
    private final LocationsJpa locations;
    private final AvailabilityBlocksJpa blocks;
    private final ProfessionalSlotsJpa slots;
    private final AppointmentsJpa appointments;
    private final AppointmentStatusesJpa statuses;
    private final AppointmentHistoriesJpa history;
    private final AffiliationsJpa affiliations;
    private final Clock clock;

    SchedulingJpaAdapter(ProfessionalsJpa professionals, ProfessionalLocationsJpa professionalLocations,
                         SpecialtiesJpa specialties, LocationsJpa locations, AvailabilityBlocksJpa blocks,
                         ProfessionalSlotsJpa slots, AppointmentsJpa appointments, AppointmentStatusesJpa statuses,
                         AppointmentHistoriesJpa history, AffiliationsJpa affiliations, Clock clock) {
        this.professionals = professionals; this.professionalLocations = professionalLocations;
        this.specialties = specialties; this.locations = locations; this.blocks = blocks; this.slots = slots;
        this.appointments = appointments; this.statuses = statuses; this.history = history;
        this.affiliations = affiliations; this.clock = clock;
    }

    @Override @Transactional
    public Ports.AvailabilityBlockView createBlock(Long professionalUserId, Short locationId, LocalDate date,
                                                   LocalTime startTime, LocalTime endTime) {
        ProfessionalEntity professional = ownActiveProfessional(professionalUserId);
        validateBlock(professional, locationId, date, startTime, endTime, null);
        AvailabilityBlockEntity block = new AvailabilityBlockEntity();
        block.professionalId = professional.id; block.locationId = locationId; block.availableDate = date;
        block.startTime = startTime; block.endTime = endTime; block.active = true;
        AvailabilityBlockEntity saved = blocks.saveAndFlush(block);
        createSlots(saved);
        return view(saved);
    }

    @Override @Transactional(readOnly = true)
    public List<Ports.AvailabilityBlockView> blocks(Long professionalUserId, LocalDate date, Short locationId) {
        ProfessionalEntity professional = ownProfessional(professionalUserId);
        return blocks.ownBlocks(professional.id, date, locationId).stream().map(this::view).toList();
    }

    @Override @Transactional
    public Ports.AvailabilityBlockView updateBlock(Long professionalUserId, Long blockId, Short locationId,
                                                   LocalDate date, LocalTime startTime, LocalTime endTime) {
        ProfessionalEntity professional = ownActiveProfessional(professionalUserId);
        AvailabilityBlockEntity block = ownBlock(professional.id, blockId);
        ensureUncommitted(block);
        validateBlock(professional, locationId, date, startTime, endTime, block.id);
        slots.deleteAll(slots.findByBlock_Id(block.id));
        block.locationId = locationId; block.availableDate = date; block.startTime = startTime; block.endTime = endTime;
        AvailabilityBlockEntity saved = blocks.saveAndFlush(block);
        createSlots(saved);
        return view(saved);
    }

    @Override @Transactional
    public void deleteBlock(Long professionalUserId, Long blockId) {
        ProfessionalEntity professional = ownProfessional(professionalUserId);
        AvailabilityBlockEntity block = ownBlock(professional.id, blockId);
        ensureUncommitted(block);
        slots.deleteAll(slots.findByBlock_Id(block.id));
        blocks.delete(block);
    }

    @Override @Transactional(readOnly = true)
    public List<Ports.AvailabilityOption> availability(Short locationId, Short specialtyId, Long professionalId, LocalDate date) {
        if (locationId == null || specialtyId == null || date == null) throw new IllegalArgumentException("Sede, especialidad y fecha son obligatorias");
        requireFuture(date);
        SpecialtyEntity specialty = activeSpecialty(specialtyId);
        List<Long> ids = professionals.reservable(specialtyId, locationId, professionalId).stream().map(p -> p.id).toList();
        if (ids.isEmpty()) return List.of();
        Map<Long, List<ProfessionalSlotEntity>> grouped = new HashMap<>();
        for (ProfessionalSlotEntity slot : slots.freeSlots(ids, locationId, date)) {
            grouped.computeIfAbsent(slot.block.professionalId, ignored -> new ArrayList<>()).add(slot);
        }
        int needed = specialty.appointmentDurationMinutes / SLOT_MINUTES;
        List<Ports.AvailabilityOption> result = new ArrayList<>();
        for (Map.Entry<Long, List<ProfessionalSlotEntity>> entry : grouped.entrySet()) {
            List<ProfessionalSlotEntity> available = entry.getValue();
            for (int index = 0; index <= available.size() - needed; index++) {
                ProfessionalSlotEntity first = available.get(index);
                boolean consecutive = true;
                for (int offset = 1; offset < needed; offset++) {
                    if (!available.get(index + offset).startAt.equals(first.startAt.plusMinutes((long) SLOT_MINUTES * offset))) {
                        consecutive = false; break;
                    }
                }
                if (consecutive) result.add(new Ports.AvailabilityOption(entry.getKey(), locationId, specialtyId,
                        first.startAt, first.startAt.plusMinutes(specialty.appointmentDurationMinutes),
                        specialty.appointmentDurationMinutes, specialty.general));
            }
        }
        return result;
    }

    @Override @Transactional
    public Ports.AppointmentView reserve(Long patientUserId, Long professionalId, Short locationId, Short specialtyId,
                                         LocalDateTime startAt, String reason) {
        if (professionalId == null || locationId == null || specialtyId == null || startAt == null) throw new IllegalArgumentException("Selección incompleta");
        requireFuture(startAt.toLocalDate());
        if (!startAt.toLocalTime().equals(startAt.toLocalTime().withSecond(0).withNano(0)) || startAt.getMinute() % SLOT_MINUTES != 0) {
            throw new IllegalArgumentException("El horario debe iniciar en un límite de 30 minutos");
        }
        SpecialtyEntity specialty = activeSpecialty(specialtyId);
        if (professionals.reservable(specialtyId, locationId, professionalId).isEmpty()) throw new SchedulingConflict("La oferta ya no está disponible");
        int needed = specialty.appointmentDurationMinutes / SLOT_MINUTES;
        List<LocalDateTime> requestedStarts = new ArrayList<>();
        for (int i = 0; i < needed; i++) requestedStarts.add(startAt.plusMinutes((long) SLOT_MINUTES * i));
        List<ProfessionalSlotEntity> locked = slots.lockForReservation(professionalId, locationId, requestedStarts);
        if (locked.size() != needed || locked.stream().anyMatch(slot -> slot.appointmentId != null)
                || !locked.stream().map(slot -> slot.startAt).toList().equals(requestedStarts)) {
            throw new SchedulingConflict("El horario ya no está disponible");
        }
        String initialStatus = specialty.general ? "APPROVED" : "REQUESTED";
        AppointmentEntity appointment = new AppointmentEntity();
        appointment.patientUserId = patientUserId; appointment.professionalId = professionalId;
        appointment.locationId = locationId; appointment.specialtyId = specialtyId;
        appointment.insuranceAffiliationId = affiliations.findByUser_IdAndCurrentTrue(patientUserId).map(a -> a.id).orElse(null);
        appointment.statusId = status(initialStatus).id; appointment.reason = normalizedReason(reason);
        appointment.scheduledStartAt = startAt; appointment.scheduledEndAt = startAt.plusMinutes(specialty.appointmentDurationMinutes);
        appointment.createdByUserId = patientUserId;
        if (specialty.general) { appointment.approvedByUserId = null; appointment.approvedAt = LocalDateTime.now(clock); }
        AppointmentEntity saved = appointments.saveAndFlush(appointment);
        for (ProfessionalSlotEntity slot : locked) slot.appointmentId = saved.id;
        slots.saveAllAndFlush(locked);
        addHistory(saved.id, saved.statusId, specialty.general ? null : patientUserId, specialty.general ? "SYSTEM" : "USER", null);
        return appointmentView(saved, initialStatus);
    }

    @Override @Transactional(readOnly = true)
    public List<Ports.AppointmentView> requestedAppointments() {
        AppointmentStatusEntity requested = status("REQUESTED");
        return appointments.findByStatusIdOrderByScheduledStartAt(requested.id).stream()
                .map(a -> appointmentView(a, "REQUESTED")).toList();
    }

    @Override @Transactional
    public Ports.AppointmentView decide(Long adminUserId, Long appointmentId, String decision, String reason) {
        if (!"APPROVE".equals(decision) && !"REJECT".equals(decision)) throw new IllegalArgumentException("Decisión inválida");
        AppointmentEntity appointment = appointments.lockById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));
        if (appointment.statusId.shortValue() != status("REQUESTED").id.shortValue()) throw new SchedulingConflict("La cita ya fue decidida");
        if ("REJECT".equals(decision) && (reason == null || reason.isBlank())) throw new IllegalArgumentException("El rechazo exige motivo");
        String next = "APPROVE".equals(decision) ? "APPROVED" : "REJECTED";
        AppointmentStatusEntity target = status(next);
        appointment.statusId = target.id;
        appointment.reason = "REJECT".equals(decision) ? normalizedReason(reason) : appointment.reason;
        if ("APPROVE".equals(decision)) { appointment.approvedByUserId = adminUserId; appointment.approvedAt = LocalDateTime.now(clock); }
        appointments.saveAndFlush(appointment);
        if ("REJECT".equals(decision)) {
            List<ProfessionalSlotEntity> reserved = slots.findByAppointmentId(appointment.id);
            reserved.forEach(slot -> slot.appointmentId = null);
            slots.saveAllAndFlush(reserved);
        }
        addHistory(appointment.id, target.id, adminUserId, "ADMIN", "REJECT".equals(decision) ? appointment.reason : null);
        return appointmentView(appointment, next);
    }

    private ProfessionalEntity ownProfessional(Long userId) {
        return professionals.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("Profesional no encontrado"));
    }
    private ProfessionalEntity ownActiveProfessional(Long userId) {
        ProfessionalEntity professional = ownProfessional(userId);
        if (!professional.active) throw new SchedulingConflict("El profesional está inactivo");
        return professional;
    }
    private AvailabilityBlockEntity ownBlock(Long professionalId, Long blockId) {
        AvailabilityBlockEntity block = blocks.findById(blockId).orElseThrow(() -> new IllegalArgumentException("Bloque no encontrado"));
        if (!Objects.equals(block.professionalId, professionalId)) throw new SchedulingConflict("No puede modificar un bloque ajeno");
        return block;
    }
    private void validateBlock(ProfessionalEntity professional, Short locationId, LocalDate date, LocalTime start, LocalTime end, Long excludedId) {
        if (locationId == null || date == null || start == null || end == null) throw new IllegalArgumentException("Datos de bloque incompletos");
        requireFuture(date); validateTimeRange(start, end);
        if (locations.findById(locationId).filter(l -> l.active).isEmpty()
                || !professionalLocations.existsById(new ProfessionalLocationKey(professional.id, locationId))) {
            throw new SchedulingConflict("El profesional no está habilitado en esa sede");
        }
        if (!blocks.overlaps(professional.id, date, start, end, excludedId).isEmpty()) throw new SchedulingConflict("El bloque se solapa con otro publicado");
    }
    private void validateTimeRange(LocalTime start, LocalTime end) {
        if (!end.isAfter(start) || start.getSecond() != 0 || end.getSecond() != 0 || start.getNano() != 0 || end.getNano() != 0
                || start.getMinute() % SLOT_MINUTES != 0 || end.getMinute() % SLOT_MINUTES != 0
                || java.time.Duration.between(start, end).toMinutes() % SLOT_MINUTES != 0) {
            throw new IllegalArgumentException("La franja debe usar intervalos de 30 minutos");
        }
    }
    private void requireFuture(LocalDate date) {
        if (!date.isAfter(LocalDate.now(clock.withZone(BOGOTA)))) throw new IllegalArgumentException("La fecha debe ser futura");
    }
    private void ensureUncommitted(AvailabilityBlockEntity block) {
        requireFuture(block.availableDate);
        if (slots.existsByBlock_IdAndAppointmentIdIsNotNull(block.id)) throw new SchedulingConflict("El bloque tiene citas comprometidas");
    }
    private SpecialtyEntity activeSpecialty(Short id) {
        return specialties.findById(id).filter(s -> s.active).orElseThrow(() -> new IllegalArgumentException("Especialidad no disponible"));
    }
    private AppointmentStatusEntity status(String code) {
        return statuses.findByCode(code).orElseThrow(() -> new IllegalStateException("Estado faltante: " + code));
    }
    private void createSlots(AvailabilityBlockEntity block) {
        List<ProfessionalSlotEntity> generated = new ArrayList<>();
        LocalDateTime current = LocalDateTime.of(block.availableDate, block.startTime);
        LocalDateTime end = LocalDateTime.of(block.availableDate, block.endTime);
        while (current.isBefore(end)) {
            ProfessionalSlotEntity slot = new ProfessionalSlotEntity(); slot.block = block; slot.startAt = current; slot.endAt = current.plusMinutes(SLOT_MINUTES);
            generated.add(slot); current = current.plusMinutes(SLOT_MINUTES);
        }
        slots.saveAllAndFlush(generated);
    }
    private void addHistory(Long appointmentId, Short statusId, Long actorId, String source, String reason) {
        AppointmentHistoryEntity item = new AppointmentHistoryEntity(); item.appointmentId = appointmentId; item.statusId = statusId;
        item.changedByUserId = actorId; item.changeSource = source; item.reason = reason; history.save(item);
    }
    private Ports.AvailabilityBlockView view(AvailabilityBlockEntity b) {
        return new Ports.AvailabilityBlockView(b.id, b.professionalId, b.locationId, b.availableDate, b.startTime, b.endTime);
    }
    private Ports.AppointmentView appointmentView(AppointmentEntity a, String status) {
        return new Ports.AppointmentView(a.id, a.patientUserId, a.professionalId, a.locationId, a.specialtyId,
                status, a.scheduledStartAt, a.scheduledEndAt, a.reason);
    }
    private String normalizedReason(String reason) { return reason == null || reason.isBlank() ? null : reason.trim(); }
}
