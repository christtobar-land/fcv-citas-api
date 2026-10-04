package co.com.fcv.training.citas.application.medical;

import co.com.fcv.training.citas.adapter.persistence.RoleEntity;
import co.com.fcv.training.citas.adapter.persistence.RolesJpa;
import co.com.fcv.training.citas.adapter.persistence.UserEntity;
import co.com.fcv.training.citas.adapter.persistence.UsersJpa;
import co.com.fcv.training.citas.adapter.persistence.medical.*;
import co.com.fcv.training.citas.application.automation.AppointmentNotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MedicalServices {

    private final LocationRepository locationRepository;
    private final SpecialtyRepository specialtyRepository;
    private final ProfessionalRepository professionalRepository;
    private final AvailabilityBlockRepository blockRepository;
    private final ProfessionalSlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentStatusRepository statusRepository;
    private final AppointmentStatusHistoryRepository historyRepository;
    private final EpsRepository epsRepository;
    private final UserInsuranceAffiliationRepository affiliationRepository;
    private final UsersJpa userRepository;
    private final RolesJpa rolesRepository;
    private final ApplicationEventPublisher eventPublisher;

    public MedicalServices(
        LocationRepository locationRepository,
        SpecialtyRepository specialtyRepository,
        ProfessionalRepository professionalRepository,
        AvailabilityBlockRepository blockRepository,
        ProfessionalSlotRepository slotRepository,
        AppointmentRepository appointmentRepository,
        AppointmentStatusRepository statusRepository,
        AppointmentStatusHistoryRepository historyRepository,
        EpsRepository epsRepository,
        UserInsuranceAffiliationRepository affiliationRepository,
        UsersJpa userRepository,
        RolesJpa rolesRepository,
        ApplicationEventPublisher eventPublisher
    ) {
        this.locationRepository = locationRepository;
        this.specialtyRepository = specialtyRepository;
        this.professionalRepository = professionalRepository;
        this.blockRepository = blockRepository;
        this.slotRepository = slotRepository;
        this.appointmentRepository = appointmentRepository;
        this.statusRepository = statusRepository;
        this.historyRepository = historyRepository;
        this.epsRepository = epsRepository;
        this.affiliationRepository = affiliationRepository;
        this.userRepository = userRepository;
        this.rolesRepository = rolesRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publica un evento mínimo (sin datos clínicos) para notificar al paciente vía n8n.
     * Se entrega después del commit; si n8n no está configurado el evento simplemente se ignora.
     */
    private void publishNotification(String eventType, AppointmentEntity a, String reason) {
        UserEntity patient = a.getPatient();
        if (patient == null) {
            return;
        }
        eventPublisher.publishEvent(new AppointmentNotificationEvent(
            AppointmentNotificationEvent.newId(),
            eventType,
            OffsetDateTime.now(),
            a.getId(),
            a.getStatus().getCode(),
            patient.getFirstName(),
            patient.getEmail(),
            a.getSpecialty().getName(),
            a.getLocation().getName(),
            a.getScheduledStartAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            reason
        ));
    }

    // ==================== DTOs ====================

    public record LocationDto(Short id, String code, String name, String address, String city, Boolean active) {}

    public record SpecialtyDto(
        Short id, String code, String name, Short durationMinutes,
        Boolean isGeneral, Boolean requiresAdminApproval, Boolean active
    ) {}

    public record EpsDto(Long id, String code, String name, Boolean active) {}

    public record UserAffiliationDto(
        Long id, String epsName, String planName, String membershipNumber, Boolean isCurrent
    ) {}

    public record AvailableSlotDto(
        Long slotId,
        Long professionalId,
        String professionalName,
        String licenseNumber,
        Short locationId,
        String locationName,
        String startAt,
        String endAt
    ) {}

    public record AvailabilityBlockDto(
        Long id,
        Long professionalId,
        String professionalName,
        Short locationId,
        String locationName,
        String locationAddress,
        LocalDate availableDate,
        String startTime,
        String endTime,
        Boolean active,
        long totalSlots,
        long bookedSlots,
        boolean canDelete
    ) {}

    public record CreateBlockCommand(
        Long professionalId,
        Short locationId,
        LocalDate availableDate,
        LocalTime startTime,
        LocalTime endTime
    ) {}

    public record BookAppointmentCommand(
        Short locationId,
        Short specialtyId,
        Long professionalId,
        LocalDateTime scheduledStartAt,
        String reason,
        String referralCode
    ) {}

    public record RescheduleCommand(
        LocalDateTime newStartAt,
        Long professionalId,
        String reason
    ) {
        public RescheduleCommand(LocalDateTime newStartAt, String reason) {
            this(newStartAt, null, reason);
        }
    }

    public record AppointmentDto(
        Long id,
        Long professionalId,
        String professionalName,
        String licenseNumber,
        Short locationId,
        String locationName,
        String locationAddress,
        Short specialtyId,
        String specialtyName,
        String status,
        String statusCode,
        String scheduledStartAt,
        String scheduledEndAt,
        Short durationMinutes,
        String reason,
        String referralCode,
        boolean isTerminal
    ) {}

    public record ProfessionalAppointmentDto(
        Long id,
        Long patientId,
        String patientName,
        String patientDocumentType,
        String patientDocumentNumber,
        String patientPhone,
        String patientEmail,
        String epsName,
        Long professionalId,
        String professionalName,
        Short locationId,
        String locationName,
        String locationAddress,
        Short specialtyId,
        String specialtyName,
        String status,
        String statusCode,
        String scheduledStartAt,
        String scheduledEndAt,
        Short durationMinutes,
        String reason,
        String referralCode,
        boolean isTerminal
    ) {}

    public record AdminAppointmentDto(
        Long id,
        Long patientId,
        String patientName,
        String patientDocument,
        String patientPhone,
        String patientEmail,
        String epsName,
        Long professionalId,
        String professionalName,
        String professionalLicense,
        Short locationId,
        String locationName,
        String locationAddress,
        Short specialtyId,
        String specialtyName,
        String status,
        String statusCode,
        String scheduledStartAt,
        String scheduledEndAt,
        Short durationMinutes,
        String reason,
        String referralCode,
        String createdAt
    ) {}

    public record AppointmentHistoryDto(
        Long id,
        Long appointmentId,
        String statusCode,
        String statusName,
        String changeSource,
        String changedByName,
        String reason,
        String changedAt
    ) {}

    public record LocationOccupancyDto(
        Short locationId,
        String locationName,
        String locationAddress,
        long totalSlotsMonth,
        long bookedSlotsMonth,
        int occupancyPercentMonth,
        long totalSlotsToday,
        long bookedSlotsToday,
        int occupancyPercentToday,
        long activeProfessionalsCount,
        long pendingApprovalCount
    ) {}

    // ==================== CATALOGS ====================

    @Transactional(readOnly = true)
    public List<LocationDto> getLocations() {
        return locationRepository.findAllByOrderByNameAsc().stream()
            .map(l -> new LocationDto(l.getId(), l.getCode(), l.getName(), l.getAddress(), l.getCity(), l.getActive()))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<SpecialtyDto> getSpecialties() {
        return specialtyRepository.findAllByOrderByNameAsc().stream()
            .map(s -> new SpecialtyDto(
                s.getId(), s.getCode(), s.getName(), s.getAppointmentDurationMinutes(),
                s.getIsGeneral(), s.getRequiresAdminApproval(), s.getActive()
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<EpsDto> getEpsList() {
        return epsRepository.findAllByOrderByNameAsc().stream()
            .map(e -> new EpsDto(e.getId(), e.getCode(), e.getName(), e.getActive()))
            .toList();
    }

    @Transactional(readOnly = true)
    public UserAffiliationDto getUserAffiliation(Long userId) {
        return affiliationRepository.findByUserIdAndIsCurrentTrue(userId)
            .map(a -> new UserAffiliationDto(
                a.getId(),
                a.getPlan().getEps().getName(),
                a.getPlan().getName(),
                a.getMembershipNumber(),
                a.getIsCurrent()
            ))
            .orElse(new UserAffiliationDto(null, "EPS Salud Total", "Plan Contributivo", "AF-92000100", true));
    }

    public record ProfessionalDto(
        Long id, String name, String licenseNumber, String professionalCode,
        List<Short> specialtyIds, List<Short> locationIds, Boolean active
    ) {}

    public record CreateProfessionalCommand(
        String firstName,
        String lastName,
        String documentType,
        String documentNumber,
        String email,
        String phone,
        String licenseNumber,
        List<Short> specialtyIds,
        List<Short> locationIds
    ) {}

    public record CreateLocationCommand(
        String code,
        String name,
        String address,
        String city
    ) {}

    public record CreateSpecialtyCommand(
        String code,
        String name,
        Short durationMinutes,
        Boolean isGeneral,
        Boolean requiresAdminApproval
    ) {}

    public record CreateEpsCommand(
        String code,
        String name
    ) {}

    @Transactional(readOnly = true)
    public List<ProfessionalDto> getProfessionals(Short specialtyId, Short locationId) {
        List<ProfessionalEntity> professionals;
        if (specialtyId != null && locationId != null) {
            professionals = professionalRepository.findBySpecialtyAndLocation(specialtyId, locationId);
        } else {
            professionals = professionalRepository.findAll();
        }

        return professionals.stream().map(p -> {
            UserEntity u = p.getUser();
            String fullName = "Dr(a). " + (u != null ? u.getFirstName() + " " + u.getLastName() : "Profesional");
            List<Short> sIds = p.getSpecialties() != null ? p.getSpecialties().stream().map(SpecialtyEntity::getId).toList() : List.of();
            List<Short> lIds = p.getLocations() != null ? p.getLocations().stream().map(LocationEntity::getId).toList() : List.of();
            return new ProfessionalDto(p.getId(), fullName, p.getLicenseNumber(), p.getProfessionalCode(), sIds, lIds, p.getActive());
        }).toList();
    }

    // ==================== DISPONIBILIDAD ====================

    @Transactional(readOnly = true)
    public List<AvailableSlotDto> findAvailableSlots(
        Short locationId,
        Short specialtyId,
        Long professionalId,
        LocalDate date
    ) {
        if (date.isBefore(LocalDate.now())) {
            return List.of();
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dayStart = date.isEqual(LocalDate.now()) ? now : date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(23, 59, 59);

        List<ProfessionalSlotEntity> freeSlots = slotRepository.findAvailableSlots(
            locationId, specialtyId, professionalId, dayStart, dayEnd
        );

        SpecialtyEntity specialty = specialtyId != null
            ? specialtyRepository.findById(specialtyId).orElse(null)
            : null;

        int requiredMinutes = (specialty != null) ? specialty.getAppointmentDurationMinutes() : 30;

        List<AvailableSlotDto> results = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        if (requiredMinutes == 30) {
            for (ProfessionalSlotEntity s : freeSlots) {
                UserEntity u = s.getAvailabilityBlock().getProfessional().getUser();
                results.add(new AvailableSlotDto(
                    s.getId(),
                    s.getAvailabilityBlock().getProfessional().getId(),
                    "Dr(a). " + u.getFirstName() + " " + u.getLastName(),
                    s.getAvailabilityBlock().getProfessional().getLicenseNumber(),
                    s.getAvailabilityBlock().getLocation().getId(),
                    s.getAvailabilityBlock().getLocation().getName(),
                    s.getStartAt().format(dtf),
                    s.getEndAt().format(dtf)
                ));
            }
        } else if (requiredMinutes == 60) {
            // Requiere 2 slots consecutivos de 30 min
            for (int i = 0; i < freeSlots.size() - 1; i++) {
                ProfessionalSlotEntity s1 = freeSlots.get(i);
                ProfessionalSlotEntity s2 = freeSlots.get(i + 1);

                if (s1.getAvailabilityBlock().getProfessional().getId().equals(s2.getAvailabilityBlock().getProfessional().getId())
                    && s1.getEndAt().equals(s2.getStartAt())) {
                    UserEntity u = s1.getAvailabilityBlock().getProfessional().getUser();
                    results.add(new AvailableSlotDto(
                        s1.getId(),
                        s1.getAvailabilityBlock().getProfessional().getId(),
                        "Dr(a). " + u.getFirstName() + " " + u.getLastName(),
                        s1.getAvailabilityBlock().getProfessional().getLicenseNumber(),
                        s1.getAvailabilityBlock().getLocation().getId(),
                        s1.getAvailabilityBlock().getLocation().getName(),
                        s1.getStartAt().format(dtf),
                        s2.getEndAt().format(dtf)
                    ));
                }
            }
        }

        return results;
    }

    // ==================== GESTIÓN DE CITAS ====================

    @Transactional
    public List<AppointmentDto> getMyAppointments(Long patientUserId) {
        reconcilePastUnattendedAppointments();
        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return appointmentRepository.findByPatientUserIdOrderByScheduledStartAtDesc(patientUserId).stream()
            .map(a -> {
                UserEntity u = a.getProfessional().getUser();
                short dur = (short) java.time.Duration.between(a.getScheduledStartAt(), a.getScheduledEndAt()).toMinutes();
                return new AppointmentDto(
                    a.getId(),
                    a.getProfessional().getId(),
                    "Dr(a). " + u.getFirstName() + " " + u.getLastName(),
                    a.getProfessional().getLicenseNumber(),
                    a.getLocation().getId(),
                    a.getLocation().getName(),
                    a.getLocation().getAddress(),
                    a.getSpecialty().getId(),
                    a.getSpecialty().getName(),
                    a.getStatus().getName(),
                    a.getStatus().getCode(),
                    a.getScheduledStartAt().format(dtf),
                    a.getScheduledEndAt().format(dtf),
                    dur,
                    a.getReason(),
                    a.getReferralCode(),
                    a.getStatus().getIsTerminal()
                );
            })
            .toList();
    }

    @Transactional
    public AppointmentDto bookAppointment(Long patientUserId, BookAppointmentCommand cmd) {
        UserEntity patient = userRepository.findById(patientUserId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario paciente no encontrado"));

        SpecialtyEntity specialty = specialtyRepository.findById(cmd.specialtyId())
            .orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada"));

        ProfessionalEntity professional = professionalRepository.findById(cmd.professionalId())
            .orElseThrow(() -> new IllegalArgumentException("Profesional no encontrado"));

        LocationEntity location = locationRepository.findById(cmd.locationId())
            .orElseThrow(() -> new IllegalArgumentException("Sede no encontrada"));

        int duration = specialty.getAppointmentDurationMinutes();
        LocalDateTime startAt = cmd.scheduledStartAt();
        LocalDateTime endAt = startAt.plusMinutes(duration);

        if (startAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se pueden agendar citas en el pasado.");
        }

        // Validar que el paciente no tenga ya una cita activa en esta misma fecha y horario
        if (appointmentRepository.existsActiveAppointmentForPatientInRange(patient.getId(), startAt, endAt)) {
            throw new IllegalArgumentException("Ya cuentas con una cita médica activa programada en esta misma fecha y horario. Por favor selecciona otra franja horaria.");
        }

        // Bloqueo pesimista de slots para impedir concurrencia / doble reserva
        List<ProfessionalSlotEntity> lockedSlots = slotRepository.lockFreeSlotsForRange(
            professional.getId(), location.getId(), startAt, endAt
        );

        int expectedSlots = duration / 30;
        if (lockedSlots.size() != expectedSlots) {
            throw new IllegalArgumentException("La franja horaria seleccionada ya no está disponible. Por favor seleccione otro horario.");
        }

        // Estado inicial de la cita:
        // Medicina General -> APPROVED automáticamente (agendamiento directo).
        // Especializada (requiere validación de volante EPS) -> REQUESTED (en revisión administrativa).
        String statusCode;
        if (Boolean.TRUE.equals(specialty.getIsGeneral())) {
            statusCode = "APPROVED";
        } else if (Boolean.TRUE.equals(specialty.getRequiresAdminApproval()) || !Boolean.TRUE.equals(specialty.getIsGeneral())) {
            statusCode = "REQUESTED";
        } else {
            statusCode = "APPROVED";
        }

        AppointmentStatusEntity status = statusRepository.findByCode(statusCode)
            .orElseThrow(() -> new IllegalStateException("Estado de cita no encontrado: " + statusCode));

        AppointmentEntity appointment = new AppointmentEntity();
        appointment.setPatient(patient);
        appointment.setProfessional(professional);
        appointment.setLocation(location);
        appointment.setSpecialty(specialty);
        appointment.setStatus(status);
        appointment.setReason(cmd.reason());
        appointment.setReferralCode(cmd.referralCode());
        appointment.setScheduledStartAt(startAt);
        appointment.setScheduledEndAt(endAt);
        appointment.setCreatedBy(patient);

        if ("APPROVED".equals(statusCode)) {
            appointment.setApprovedAt(LocalDateTime.now());
        }

        AppointmentEntity saved = appointmentRepository.save(appointment);

        // Asignar slots a la cita
        for (ProfessionalSlotEntity slot : lockedSlots) {
            slot.setAppointmentId(saved.getId());
            slotRepository.save(slot);
        }

        // Registrar en historial de auditoría
        AppointmentStatusHistoryEntity history = new AppointmentStatusHistoryEntity();
        history.setAppointment(saved);
        history.setStatus(status);
        history.setChangedBy(patient);
        history.setChangeSource("USER");
        history.setReason("Creación inicial de la cita");
        historyRepository.save(history);
        if ("APPROVED".equals(statusCode)) {
            publishNotification(AppointmentNotificationEvent.APPROVED, saved, null);
        }

        UserEntity profUser = professional.getUser();
        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return new AppointmentDto(
            saved.getId(),
            professional.getId(),
            "Dr(a). " + profUser.getFirstName() + " " + profUser.getLastName(),
            professional.getLicenseNumber(),
            location.getId(),
            location.getName(),
            location.getAddress(),
            specialty.getId(),
            specialty.getName(),
            status.getName(),
            status.getCode(),
            saved.getScheduledStartAt().format(dtf),
            saved.getScheduledEndAt().format(dtf),
            (short) duration,
            saved.getReason(),
            saved.getReferralCode(),
            status.getIsTerminal()
        );
    }

    @Transactional
    public AppointmentDto rescheduleAppointment(Long patientUserId, Long appointmentId, RescheduleCommand cmd) {
        AppointmentEntity appointment = appointmentRepository.findByIdAndPatientUserId(appointmentId, patientUserId)
            .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada o no pertenece al paciente"));

        if (Boolean.TRUE.equals(appointment.getStatus().getIsTerminal())) {
            throw new IllegalStateException("No se puede reprogramar una cita en estado terminal (" + appointment.getStatus().getName() + ").");
        }

        LocalDateTime newStart = cmd.newStartAt();
        int duration = appointment.getSpecialty().getAppointmentDurationMinutes();
        LocalDateTime newEnd = newStart.plusMinutes(duration);

        if (newStart.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La nueva fecha no puede ser en el pasado.");
        }

        Long targetProfId = (cmd.professionalId() != null)
            ? cmd.professionalId()
            : appointment.getProfessional().getId();

        ProfessionalEntity targetProf = (cmd.professionalId() != null && !cmd.professionalId().equals(appointment.getProfessional().getId()))
            ? professionalRepository.findById(cmd.professionalId())
                .orElseThrow(() -> new IllegalArgumentException("Profesional no encontrado"))
            : appointment.getProfessional();

        // Validar que el paciente no tenga ya otra cita activa en la nueva franja horaria
        if (appointmentRepository.existsActiveAppointmentForPatientInRangeExcluding(patientUserId, appointmentId, newStart, newEnd)) {
            throw new IllegalArgumentException("Ya cuentas con otra cita médica programada en esta misma fecha y franja horaria.");
        }

        // Bloqueo pesimista para la nueva franja con el profesional correspondiente
        List<ProfessionalSlotEntity> newSlots = slotRepository.lockFreeSlotsForRange(
            targetProfId, appointment.getLocation().getId(), newStart, newEnd
        );

        int expectedSlots = duration / 30;
        if (newSlots.size() != expectedSlots) {
            throw new IllegalArgumentException("La nueva franja seleccionada ya no se encuentra disponible. Por favor seleccione otro horario.");
        }

        // REPROGRAMACIÓN AUTOMÁTICA INMEDIATA:
        // 1. Liberar slots anteriores
        List<ProfessionalSlotEntity> oldSlots = slotRepository.findByAppointmentId(appointmentId);
        for (ProfessionalSlotEntity oldSlot : oldSlots) {
            oldSlot.setAppointmentId(null);
            slotRepository.save(oldSlot);
        }

        // 2. Asignar los nuevos slots
        for (ProfessionalSlotEntity newSlot : newSlots) {
            newSlot.setAppointmentId(appointmentId);
            slotRepository.save(newSlot);
        }

        // 3. Actualizar cita
        AppointmentStatusEntity approvedStatus = statusRepository.findByCode("APPROVED")
            .orElse(appointment.getStatus());

        appointment.setScheduledStartAt(newStart);
        appointment.setScheduledEndAt(newEnd);
        appointment.setProfessional(targetProf);
        appointment.setLocation(newSlots.get(0).getAvailabilityBlock().getLocation());
        appointment.setStatus(approvedStatus);
        appointment.setApprovedAt(LocalDateTime.now());
        AppointmentEntity updated = appointmentRepository.save(appointment);

        // 4. Auditoría
        AppointmentStatusHistoryEntity history = new AppointmentStatusHistoryEntity();
        history.setAppointment(updated);
        history.setStatus(approvedStatus);
        history.setChangedBy(appointment.getPatient());
        history.setChangeSource("USER");
        history.setReason(cmd.reason() != null && !cmd.reason().isBlank()
            ? cmd.reason()
            : "Reprogramación automática inmediata a nueva franja horaria");
        historyRepository.save(history);
        publishNotification(AppointmentNotificationEvent.RESCHEDULED, updated, cmd.reason());

        UserEntity profUser = updated.getProfessional().getUser();
        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return new AppointmentDto(
            updated.getId(),
            updated.getProfessional().getId(),
            "Dr(a). " + profUser.getFirstName() + " " + profUser.getLastName(),
            updated.getProfessional().getLicenseNumber(),
            updated.getLocation().getId(),
            updated.getLocation().getName(),
            updated.getLocation().getAddress(),
            updated.getSpecialty().getId(),
            updated.getSpecialty().getName(),
            updated.getStatus().getName(),
            updated.getStatus().getCode(),
            updated.getScheduledStartAt().format(dtf),
            updated.getScheduledEndAt().format(dtf),
            (short) duration,
            updated.getReason(),
            updated.getReferralCode(),
            updated.getStatus().getIsTerminal()
        );
    }

    @Transactional
    public void cancelAppointment(Long patientUserId, Long appointmentId, String reason) {
        AppointmentEntity appointment = appointmentRepository.findByIdAndPatientUserId(appointmentId, patientUserId)
            .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada o no pertenece al paciente"));

        if (Boolean.TRUE.equals(appointment.getStatus().getIsTerminal())) {
            throw new IllegalStateException("La cita ya se encuentra en un estado final (" + appointment.getStatus().getName() + ").");
        }

        // Liberar slots para que otros pacientes puedan agendarlos de inmediato
        List<ProfessionalSlotEntity> slots = slotRepository.findByAppointmentId(appointmentId);
        for (ProfessionalSlotEntity slot : slots) {
            slot.setAppointmentId(null);
            slotRepository.save(slot);
        }

        AppointmentStatusEntity cancelledStatus = statusRepository.findByCode("CANCELLED")
            .orElseThrow(() -> new IllegalStateException("Estado CANCELLED no encontrado"));

        appointment.setStatus(cancelledStatus);
        appointmentRepository.save(appointment);

        // Auditoría
        AppointmentStatusHistoryEntity history = new AppointmentStatusHistoryEntity();
        history.setAppointment(appointment);
        history.setStatus(cancelledStatus);
        history.setChangedBy(appointment.getPatient());
        history.setChangeSource("USER");
        history.setReason(reason != null && !reason.isBlank() ? reason : "Cancelación voluntaria por el paciente");
        historyRepository.save(history);
        publishNotification(AppointmentNotificationEvent.CANCELLED, appointment, reason);
    }

    // ==================== AUTO-CIERRE DE CITAS NO ASISTIDAS (VENCIDAS) ====================

    @Transactional
    public void reconcilePastUnattendedAppointments() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        List<AppointmentEntity> pastUnclosed = appointmentRepository.findPastUnclosedAppointments(startOfToday);
        if (pastUnclosed.isEmpty()) return;

        AppointmentStatusEntity noShowStatus = statusRepository.findByCode("NO_SHOW").orElse(null);
        if (noShowStatus == null) return;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (AppointmentEntity apt : pastUnclosed) {
            apt.setStatus(noShowStatus);
            appointmentRepository.save(apt);

            AppointmentStatusHistoryEntity history = new AppointmentStatusHistoryEntity();
            history.setAppointment(apt);
            history.setStatus(noShowStatus);
            history.setChangedBy(null);
            history.setChangeSource("SYSTEM");
            history.setReason("Cierre automático del sistema: Cita no atendida ni cerrada durante la jornada del " + apt.getScheduledStartAt().format(dtf));
            historyRepository.save(history);
        }
    }

    // ==================== AGENDA MÉDICA (RF-16, RF-17, HU-033) ====================

    @Transactional
    public List<ProfessionalAppointmentDto> getProfessionalAgenda(Long currentUserId, Long requestedProfessionalId, LocalDate date) {
        return getProfessionalAgenda(currentUserId, requestedProfessionalId, date, null);
    }

    @Transactional
    public List<ProfessionalAppointmentDto> getProfessionalAgenda(Long currentUserId, Long requestedProfessionalId, LocalDate date, Short locationId) {
        reconcilePastUnattendedAppointments();
        Long targetProfId = requestedProfessionalId;
        if (targetProfId == null) {
            targetProfId = professionalRepository.findByUserId(currentUserId)
                .map(ProfessionalEntity::getId)
                .orElseThrow(() -> new IllegalStateException("El usuario no tiene un perfil médico asociado."));
        }

        LocalDateTime startAt = (date != null) ? date.atStartOfDay() : null;
        LocalDateTime endAt = (date != null) ? date.atTime(23, 59, 59) : null;

        List<AppointmentEntity> appointments = appointmentRepository.findByProfessionalIdAndDateRangeAndLocation(targetProfId, locationId, startAt, endAt);
        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        return appointments.stream().map(a -> {
            UserEntity patient = a.getPatient();
            String patientName = patient != null ? patient.getFirstName() + " " + patient.getLastName() : "Paciente";
            String docType = patient != null ? patient.getDocumentType() : "CC";
            String docNum = patient != null ? patient.getDocumentNumber() : "";
            String phone = patient != null ? patient.getPhone() : "";
            String email = patient != null ? patient.getEmail() : "";

            String epsName = "EPS Salud Total";
            if (patient != null) {
                epsName = affiliationRepository.findByUserIdAndIsCurrentTrue(patient.getId())
                    .map(aff -> aff.getPlan().getEps().getName())
                    .orElse("EPS Salud Total");
            }

            short dur = (short) java.time.Duration.between(a.getScheduledStartAt(), a.getScheduledEndAt()).toMinutes();
            UserEntity profUser = a.getProfessional().getUser();
            String profName = "Dr(a). " + (profUser != null ? profUser.getFirstName() + " " + profUser.getLastName() : "");

            return new ProfessionalAppointmentDto(
                a.getId(),
                patient != null ? patient.getId() : null,
                patientName,
                docType,
                docNum,
                phone,
                email,
                epsName,
                a.getProfessional().getId(),
                profName,
                a.getLocation().getId(),
                a.getLocation().getName(),
                a.getLocation().getAddress(),
                a.getSpecialty().getId(),
                a.getSpecialty().getName(),
                a.getStatus().getName(),
                a.getStatus().getCode(),
                a.getScheduledStartAt().format(dtf),
                a.getScheduledEndAt().format(dtf),
                dur,
                a.getReason(),
                a.getReferralCode(),
                a.getStatus().getIsTerminal()
            );
        }).toList();
    }

    @Transactional
    public void completeAppointment(Long actorUserId, Long appointmentId, String notes) {
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        if (Boolean.TRUE.equals(appointment.getStatus().getIsTerminal())) {
            throw new IllegalStateException("La cita ya se encuentra en un estado terminal (" + appointment.getStatus().getName() + ").");
        }

        AppointmentStatusEntity completedStatus = statusRepository.findByCode("COMPLETED")
            .orElseThrow(() -> new IllegalStateException("Estado COMPLETED no encontrado"));

        appointment.setStatus(completedStatus);
        appointmentRepository.save(appointment);

        UserEntity actor = userRepository.findById(actorUserId).orElse(null);

        AppointmentStatusHistoryEntity history = new AppointmentStatusHistoryEntity();
        history.setAppointment(appointment);
        history.setStatus(completedStatus);
        history.setChangedBy(actor);
        history.setChangeSource("USER");
        history.setReason(notes != null && !notes.isBlank() ? notes : "Atención médica finalizada con éxito");
        historyRepository.save(history);
    }

    @Transactional
    public void noShowAppointment(Long actorUserId, Long appointmentId, String reason) {
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        if (Boolean.TRUE.equals(appointment.getStatus().getIsTerminal())) {
            throw new IllegalStateException("La cita ya se encuentra en un estado terminal (" + appointment.getStatus().getName() + ").");
        }

        // En inasistencia (NO_SHOW), el tiempo/franja del profesional ya fue consumido durante el turno;
        // los slots se mantienen asociados a la cita para trazabilidad asistencial y ocupación de agenda.
        AppointmentStatusEntity noShowStatus = statusRepository.findByCode("NO_SHOW")
            .orElseThrow(() -> new IllegalStateException("Estado NO_SHOW no encontrado"));

        appointment.setStatus(noShowStatus);
        appointmentRepository.save(appointment);

        UserEntity actor = userRepository.findById(actorUserId).orElse(null);

        AppointmentStatusHistoryEntity history = new AppointmentStatusHistoryEntity();
        history.setAppointment(appointment);
        history.setStatus(noShowStatus);
        history.setChangedBy(actor);
        history.setChangeSource("USER");
        history.setReason(reason != null && !reason.isBlank() ? reason : "Paciente no se presentó a la consulta médica");
        historyRepository.save(history);
    }

    // ==================== GESTIÓN ADMINISTRADOR (RF-12, RF-18) ====================

    @Transactional(readOnly = true)
    public List<AdminAppointmentDto> getAdminPendingRequests() {
        List<AppointmentEntity> pendingList = appointmentRepository.findByStatusCodeIn(List.of("REQUESTED", "PENDING"));
        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        return pendingList.stream().map(a -> {
            UserEntity patient = a.getPatient();
            String patientName = patient != null ? patient.getFirstName() + " " + patient.getLastName() : "Paciente";
            String doc = (patient != null && patient.getDocumentType() != null ? patient.getDocumentType() + " " : "")
                + (patient != null && patient.getDocumentNumber() != null ? patient.getDocumentNumber() : "");
            String phone = patient != null ? patient.getPhone() : "";
            String email = patient != null ? patient.getEmail() : "";

            String epsName = "EPS Salud Total";
            if (patient != null) {
                epsName = affiliationRepository.findByUserIdAndIsCurrentTrue(patient.getId())
                    .map(aff -> aff.getPlan().getEps().getName())
                    .orElse("EPS Salud Total");
            }

            UserEntity profUser = a.getProfessional().getUser();
            String profName = "Dr(a). " + (profUser != null ? profUser.getFirstName() + " " + profUser.getLastName() : "");
            short dur = (short) java.time.Duration.between(a.getScheduledStartAt(), a.getScheduledEndAt()).toMinutes();
            String createdAtStr = a.getCreatedAt() != null ? a.getCreatedAt().format(dtf) : a.getScheduledStartAt().format(dtf);

            return new AdminAppointmentDto(
                a.getId(),
                patient != null ? patient.getId() : null,
                patientName,
                doc,
                phone,
                email,
                epsName,
                a.getProfessional().getId(),
                profName,
                a.getProfessional().getLicenseNumber(),
                a.getLocation().getId(),
                a.getLocation().getName(),
                a.getLocation().getAddress(),
                a.getSpecialty().getId(),
                a.getSpecialty().getName(),
                a.getStatus().getName(),
                a.getStatus().getCode(),
                a.getScheduledStartAt().format(dtf),
                a.getScheduledEndAt().format(dtf),
                dur,
                a.getReason(),
                a.getReferralCode(),
                createdAtStr
            );
        }).toList();
    }

    @Transactional
    public void approveSpecializedAppointment(Long adminUserId, Long appointmentId) {
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        if (Boolean.TRUE.equals(appointment.getStatus().getIsTerminal())) {
            throw new IllegalStateException("La cita ya se encuentra en un estado terminal (" + appointment.getStatus().getName() + ").");
        }

        AppointmentStatusEntity approvedStatus = statusRepository.findByCode("APPROVED")
            .orElseThrow(() -> new IllegalStateException("Estado APPROVED no encontrado"));

        UserEntity admin = userRepository.findById(adminUserId).orElse(null);

        appointment.setStatus(approvedStatus);
        appointment.setApprovedBy(admin);
        appointment.setApprovedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);

        AppointmentStatusHistoryEntity history = new AppointmentStatusHistoryEntity();
        history.setAppointment(appointment);
        history.setStatus(approvedStatus);
        history.setChangedBy(admin);
        history.setChangeSource("ADMIN");
        history.setReason("Aprobación y confirmación de cupo especializado por administración médica");
        historyRepository.save(history);
        publishNotification(AppointmentNotificationEvent.APPROVED, appointment, null);
    }

    @Transactional
    public void rejectSpecializedAppointment(Long adminUserId, Long appointmentId, String reason) {
        if (reason == null || reason.trim().isBlank()) {
            throw new IllegalArgumentException("El motivo de rechazo es obligatorio según el protocolo clínico.");
        }

        AppointmentEntity appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        if (Boolean.TRUE.equals(appointment.getStatus().getIsTerminal())) {
            throw new IllegalStateException("La cita ya se encuentra en un estado terminal (" + appointment.getStatus().getName() + ").");
        }

        // Liberar slots bloqueados de disponibilidad
        List<ProfessionalSlotEntity> slots = slotRepository.findByAppointmentId(appointmentId);
        for (ProfessionalSlotEntity slot : slots) {
            slot.setAppointmentId(null);
            slotRepository.save(slot);
        }

        AppointmentStatusEntity rejectedStatus = statusRepository.findByCode("REJECTED")
            .orElseThrow(() -> new IllegalStateException("Estado REJECTED no encontrado"));

        UserEntity admin = userRepository.findById(adminUserId).orElse(null);

        appointment.setStatus(rejectedStatus);
        appointmentRepository.save(appointment);

        AppointmentStatusHistoryEntity history = new AppointmentStatusHistoryEntity();
        history.setAppointment(appointment);
        history.setStatus(rejectedStatus);
        history.setChangedBy(admin);
        history.setChangeSource("ADMIN");
        history.setReason(reason.trim());
        historyRepository.save(history);
        publishNotification(AppointmentNotificationEvent.REJECTED, appointment, reason.trim());
    }

    @Transactional(readOnly = true)
    public List<LocationOccupancyDto> getAdminLocationsOccupancy() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        LocalDate firstDayMonth = today.withDayOfMonth(1);
        LocalDate lastDayMonth = today.withDayOfMonth(today.lengthOfMonth());
        LocalDateTime monthStart = firstDayMonth.atStartOfDay();
        LocalDateTime monthEnd = lastDayMonth.atTime(LocalTime.MAX);

        return locationRepository.findAllByActiveTrueOrderByNameAsc().stream()
            .map(loc -> {
                Short locId = loc.getId();
                long totalMonth = slotRepository.countByLocationIdAndDateRange(locId, monthStart, monthEnd);
                long bookedMonth = slotRepository.countBookedByLocationIdAndDateRange(locId, monthStart, monthEnd);
                int pctMonth = totalMonth > 0 ? (int) Math.round((bookedMonth * 100.0) / totalMonth) : 0;

                long totalToday = slotRepository.countByLocationIdAndDateRange(locId, todayStart, todayEnd);
                long bookedToday = slotRepository.countBookedByLocationIdAndDateRange(locId, todayStart, todayEnd);
                int pctToday = totalToday > 0 ? (int) Math.round((bookedToday * 100.0) / totalToday) : 0;

                long profs = slotRepository.countDistinctProfessionalsByLocationId(locId);
                long pending = appointmentRepository.countPendingApprovalByLocationId(locId);

                return new LocationOccupancyDto(
                    loc.getId(),
                    loc.getName(),
                    loc.getAddress(),
                    totalMonth,
                    bookedMonth,
                    pctMonth,
                    totalToday,
                    bookedToday,
                    pctToday,
                    profs,
                    pending
                );
            })
            .toList();
    }

    @Transactional
    public ProfessionalDto createProfessional(CreateProfessionalCommand cmd) {
        if (cmd.firstName() == null || cmd.firstName().trim().isBlank()) {
            throw new IllegalArgumentException("El nombre del profesional es obligatorio");
        }
        if (cmd.lastName() == null || cmd.lastName().trim().isBlank()) {
            throw new IllegalArgumentException("El apellido del profesional es obligatorio");
        }
        if (cmd.documentNumber() == null || cmd.documentNumber().trim().isBlank()) {
            throw new IllegalArgumentException("El número de documento es obligatorio");
        }
        if (cmd.email() == null || cmd.email().trim().isBlank()) {
            throw new IllegalArgumentException("El correo institucional es obligatorio");
        }
        if (cmd.licenseNumber() == null || cmd.licenseNumber().trim().isBlank()) {
            throw new IllegalArgumentException("La matrícula profesional es obligatoria");
        }

        String email = cmd.email().trim().toLowerCase();
        String docType = cmd.documentType() != null && !cmd.documentType().isBlank() ? cmd.documentType().trim().toUpperCase() : "CC";
        String docNum = cmd.documentNumber().trim();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con el correo " + email);
        }
        if (userRepository.existsByDocumentTypeAndDocumentNumber(docType, docNum)) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con el documento " + docType + " " + docNum);
        }

        RoleEntity profRole = rolesRepository.findByCode("PROFESSIONAL")
            .orElseGet(() -> rolesRepository.findAll().stream().findFirst().orElseThrow());

        UserEntity user = new UserEntity();
        user.setFirstName(cmd.firstName().trim());
        user.setLastName(cmd.lastName().trim());
        user.setDocumentType(docType);
        user.setDocumentNumber(docNum);
        user.setEmail(email);
        user.setPhone(cmd.phone() != null && !cmd.phone().isBlank() ? cmd.phone().trim() : "3001234567");
        user.setPasswordHash("$2a$10$JbKUdj4IbDZJnCk1cipwLORCojzn05ovtkcmYdGC21UMkUstm6z/6");
        user.setCreatedAt(java.time.Instant.now());
        user.setRoles(java.util.Set.of(profRole));
        UserEntity savedUser = userRepository.save(user);

        String code = "MED-" + (1000 + professionalRepository.count() + 1);

        ProfessionalEntity prof = new ProfessionalEntity();
        prof.setUser(savedUser);
        prof.setProfessionalCode(code);
        prof.setLicenseNumber(cmd.licenseNumber().trim().toUpperCase());
        prof.setActive(true);

        if (cmd.specialtyIds() != null && !cmd.specialtyIds().isEmpty()) {
            prof.setSpecialties(specialtyRepository.findAllById(cmd.specialtyIds()));
        } else {
            specialtyRepository.findById((short) 1).ifPresent(s -> prof.setSpecialties(List.of(s)));
        }

        if (cmd.locationIds() != null && !cmd.locationIds().isEmpty()) {
            prof.setLocations(locationRepository.findAllById(cmd.locationIds()));
        } else {
            prof.setLocations(locationRepository.findAllByActiveTrueOrderByNameAsc());
        }

        ProfessionalEntity savedProf = professionalRepository.save(prof);

        String fullName = "Dr(a). " + savedUser.getFirstName() + " " + savedUser.getLastName();
        List<Short> sIds = savedProf.getSpecialties() != null ? savedProf.getSpecialties().stream().map(SpecialtyEntity::getId).toList() : List.of();
        List<Short> lIds = savedProf.getLocations() != null ? savedProf.getLocations().stream().map(LocationEntity::getId).toList() : List.of();

        return new ProfessionalDto(
            savedProf.getId(),
            fullName,
            savedProf.getLicenseNumber(),
            savedProf.getProfessionalCode(),
            sIds,
            lIds,
            savedProf.getActive()
        );
    }

    @Transactional
    public boolean toggleProfessionalStatus(Long professionalId) {
        ProfessionalEntity prof = professionalRepository.findById(professionalId)
            .orElseThrow(() -> new IllegalArgumentException("Profesional no encontrado"));
        boolean newStatus = !Boolean.TRUE.equals(prof.getActive());
        prof.setActive(newStatus);
        professionalRepository.save(prof);
        return newStatus;
    }

    @Transactional
    public LocationDto createLocation(CreateLocationCommand cmd) {
        if (cmd.name() == null || cmd.name().trim().isBlank()) {
            throw new IllegalArgumentException("El nombre de la sede es obligatorio");
        }
        if (cmd.code() == null || cmd.code().trim().isBlank()) {
            throw new IllegalArgumentException("El código de la sede es obligatorio");
        }
        if (cmd.address() == null || cmd.address().trim().isBlank()) {
            throw new IllegalArgumentException("La dirección de la sede es obligatoria");
        }
        if (cmd.city() == null || cmd.city().trim().isBlank()) {
            throw new IllegalArgumentException("La ciudad de la sede es obligatoria");
        }

        String normalizedCode = cmd.code().trim().toUpperCase();
        if (locationRepository.findByCode(normalizedCode).isPresent()) {
            throw new IllegalArgumentException("Ya existe una sede registrada con el código " + normalizedCode);
        }

        LocationEntity entity = new LocationEntity();
        entity.setCode(normalizedCode);
        entity.setName(cmd.name().trim());
        entity.setAddress(cmd.address().trim());
        entity.setCity(cmd.city().trim());
        entity.setDepartment("Santander");
        entity.setActive(true);

        LocationEntity saved = locationRepository.save(entity);
        return new LocationDto(saved.getId(), saved.getCode(), saved.getName(), saved.getAddress(), saved.getCity(), saved.getActive());
    }

    @Transactional
    public boolean toggleLocationStatus(Short locationId) {
        LocationEntity location = locationRepository.findById(locationId)
            .orElseThrow(() -> new IllegalArgumentException("Sede no encontrada con ID: " + locationId));
        boolean newStatus = !Boolean.TRUE.equals(location.getActive());
        location.setActive(newStatus);
        locationRepository.save(location);
        return newStatus;
    }

    @Transactional
    public SpecialtyDto createSpecialty(CreateSpecialtyCommand cmd) {
        if (cmd.name() == null || cmd.name().trim().isBlank()) {
            throw new IllegalArgumentException("El nombre de la especialidad es obligatorio");
        }
        if (cmd.code() == null || cmd.code().trim().isBlank()) {
            throw new IllegalArgumentException("El código de la especialidad es obligatorio");
        }

        String normalizedCode = cmd.code().trim().toUpperCase();
        if (specialtyRepository.findByCode(normalizedCode).isPresent()) {
            throw new IllegalArgumentException("Ya existe una especialidad con el código " + normalizedCode);
        }

        SpecialtyEntity entity = new SpecialtyEntity();
        entity.setCode(normalizedCode);
        entity.setName(cmd.name().trim());
        entity.setAppointmentDurationMinutes(cmd.durationMinutes() != null ? cmd.durationMinutes() : (short) 30);
        entity.setIsGeneral(Boolean.TRUE.equals(cmd.isGeneral()));
        entity.setRequiresAdminApproval(cmd.requiresAdminApproval() != null ? cmd.requiresAdminApproval() : true);
        entity.setActive(true);

        SpecialtyEntity saved = specialtyRepository.save(entity);
        return new SpecialtyDto(
            saved.getId(), saved.getCode(), saved.getName(), saved.getAppointmentDurationMinutes(),
            saved.getIsGeneral(), saved.getRequiresAdminApproval(), saved.getActive()
        );
    }

    @Transactional
    public boolean toggleSpecialtyStatus(Short specialtyId) {
        SpecialtyEntity spec = specialtyRepository.findById(specialtyId)
            .orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada con ID: " + specialtyId));
        boolean newStatus = !Boolean.TRUE.equals(spec.getActive());
        spec.setActive(newStatus);
        specialtyRepository.save(spec);
        return newStatus;
    }

    @Transactional
    public EpsDto createEps(CreateEpsCommand cmd) {
        if (cmd.name() == null || cmd.name().trim().isBlank()) {
            throw new IllegalArgumentException("El nombre de la entidad EPS es obligatorio");
        }
        if (cmd.code() == null || cmd.code().trim().isBlank()) {
            throw new IllegalArgumentException("El código de la EPS es obligatorio");
        }

        String normalizedCode = cmd.code().trim().toUpperCase();
        if (epsRepository.findByCode(normalizedCode).isPresent()) {
            throw new IllegalArgumentException("Ya existe una entidad EPS con el código " + normalizedCode);
        }

        EpsEntity entity = new EpsEntity();
        entity.setCode(normalizedCode);
        entity.setName(cmd.name().trim());
        entity.setActive(true);

        EpsEntity saved = epsRepository.save(entity);
        return new EpsDto(saved.getId(), saved.getCode(), saved.getName(), saved.getActive());
    }

    @Transactional
    public boolean toggleEpsStatus(Long epsId) {
        EpsEntity eps = epsRepository.findById(epsId)
            .orElseThrow(() -> new IllegalArgumentException("Entidad EPS no encontrada con ID: " + epsId));
        boolean newStatus = !Boolean.TRUE.equals(eps.getActive());
        eps.setActive(newStatus);
        epsRepository.save(eps);
        return newStatus;
    }

    // ==================== GESTIÓN DE DISPONIBILIDAD MÉDICA (RF-08) ====================

    @Transactional(readOnly = true)
    public List<AvailabilityBlockDto> getProfessionalBlocks(Long currentUserId, Long requestedProfessionalId) {
        Long targetProfId = requestedProfessionalId;
        if (targetProfId == null) {
            targetProfId = professionalRepository.findByUserId(currentUserId)
                .map(ProfessionalEntity::getId)
                .orElseThrow(() -> new IllegalStateException("El usuario no tiene un perfil médico asociado."));
        }

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        List<AvailabilityBlockEntity> blocks = blockRepository
            .findByProfessionalIdAndActiveTrueOrderByAvailableDateAscStartTimeAsc(targetProfId);

        return blocks.stream().map(b -> {
            long totalSlots = slotRepository.countByAvailabilityBlockId(b.getId());
            long bookedSlots = slotRepository.countByAvailabilityBlockIdAndAppointmentIdIsNotNull(b.getId());
            boolean canDelete = (bookedSlots == 0);
            UserEntity u = b.getProfessional().getUser();
            String profName = "Dr(a). " + (u != null ? u.getFirstName() + " " + u.getLastName() : "");

            return new AvailabilityBlockDto(
                b.getId(),
                b.getProfessional().getId(),
                profName,
                b.getLocation().getId(),
                b.getLocation().getName(),
                b.getLocation().getAddress(),
                b.getAvailableDate(),
                b.getStartTime().format(timeFormatter),
                b.getEndTime().format(timeFormatter),
                b.getActive(),
                totalSlots,
                bookedSlots,
                canDelete
            );
        }).toList();
    }

    @Transactional
    public AvailabilityBlockDto createAvailabilityBlock(Long currentUserId, CreateBlockCommand cmd) {
        Long targetProfId = cmd.professionalId();
        if (targetProfId == null) {
            targetProfId = professionalRepository.findByUserId(currentUserId)
                .map(ProfessionalEntity::getId)
                .orElseThrow(() -> new IllegalStateException("El usuario no tiene un perfil médico asociado."));
        }

        ProfessionalEntity professional = professionalRepository.findById(targetProfId)
            .orElseThrow(() -> new IllegalArgumentException("Profesional de la salud no encontrado."));

        LocationEntity location = locationRepository.findById(cmd.locationId())
            .orElseThrow(() -> new IllegalArgumentException("Sede médica no encontrada."));

        // Validar que el profesional esté asignado a la sede seleccionada
        boolean assignedToLocation = professional.getLocations() != null &&
            professional.getLocations().stream().anyMatch(l -> l.getId().equals(location.getId()));
        if (!assignedToLocation) {
            throw new IllegalArgumentException("El profesional no se encuentra asignado a la sede seleccionada (" + location.getName() + ").");
        }

        // Validar fecha futura o hoy
        if (cmd.availableDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se pueden registrar bloques de disponibilidad en fechas del pasado.");
        }

        // Validar coherencia horaria
        if (!cmd.endTime().isAfter(cmd.startTime())) {
            throw new IllegalArgumentException("La hora de finalización debe ser posterior a la hora de inicio.");
        }

        long minutes = java.time.Duration.between(cmd.startTime(), cmd.endTime()).toMinutes();
        if (minutes < 30 || minutes % 30 != 0) {
            throw new IllegalArgumentException("La duración del bloque debe ser múltiplo de 30 minutos (mínimo 30 minutos).");
        }

        // Validar que no se traslape con otro bloque activo del mismo profesional
        List<AvailabilityBlockEntity> overlaps = blockRepository.findOverlappingBlocks(
            professional.getId(), cmd.availableDate(), cmd.startTime(), cmd.endTime()
        );
        if (!overlaps.isEmpty()) {
            throw new IllegalArgumentException("Ya existe una franja horaria activa para este profesional que se cruza con el horario seleccionado.");
        }

        // Registrar bloque
        AvailabilityBlockEntity block = new AvailabilityBlockEntity();
        block.setProfessional(professional);
        block.setLocation(location);
        block.setAvailableDate(cmd.availableDate());
        block.setStartTime(cmd.startTime());
        block.setEndTime(cmd.endTime());
        block.setActive(true);
        AvailabilityBlockEntity savedBlock = blockRepository.save(block);

        // Generar slots de 30 minutos
        LocalTime slotCursor = cmd.startTime();
        long slotsCount = 0;
        while (slotCursor.isBefore(cmd.endTime())) {
            LocalTime nextCursor = slotCursor.plusMinutes(30);
            ProfessionalSlotEntity slot = new ProfessionalSlotEntity();
            slot.setAvailabilityBlock(savedBlock);
            slot.setStartAt(LocalDateTime.of(cmd.availableDate(), slotCursor));
            slot.setEndAt(LocalDateTime.of(cmd.availableDate(), nextCursor));
            slot.setAppointmentId(null);
            slotRepository.save(slot);
            slotCursor = nextCursor;
            slotsCount++;
        }

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        UserEntity u = professional.getUser();
        String profName = "Dr(a). " + (u != null ? u.getFirstName() + " " + u.getLastName() : "");

        return new AvailabilityBlockDto(
            savedBlock.getId(),
            professional.getId(),
            profName,
            location.getId(),
            location.getName(),
            location.getAddress(),
            savedBlock.getAvailableDate(),
            savedBlock.getStartTime().format(timeFormatter),
            savedBlock.getEndTime().format(timeFormatter),
            savedBlock.getActive(),
            slotsCount,
            0L,
            true
        );
    }

    @Transactional
    public void deleteAvailabilityBlock(Long currentUserId, Long blockId) {
        AvailabilityBlockEntity block = blockRepository.findById(blockId)
            .orElseThrow(() -> new IllegalArgumentException("Franja de disponibilidad no encontrada."));

        long booked = slotRepository.countByAvailabilityBlockIdAndAppointmentIdIsNotNull(blockId);
        if (booked > 0) {
            throw new IllegalStateException("No es posible eliminar la franja de disponibilidad porque contiene " + booked + " cita(s) reservada(s) por pacientes.");
        }

        slotRepository.deleteByAvailabilityBlockId(blockId);
        blockRepository.delete(block);
    }

    // ==================== AUDITORÍA Y TRAZABILIDAD (HU-032) ====================

    @Transactional(readOnly = true)
    public List<AppointmentHistoryDto> getAppointmentHistory(Long currentUserId, Long appointmentId) {
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new IllegalArgumentException("Cita médica no encontrada."));

        UserEntity currentUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no autenticado"));

        boolean isAdmin = currentUser.getRoles().stream()
            .anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getCode()));
        boolean isPatient = appointment.getPatient() != null && appointment.getPatient().getId().equals(currentUserId);
        boolean isDoctor = appointment.getProfessional() != null &&
            appointment.getProfessional().getUser() != null &&
            appointment.getProfessional().getUser().getId().equals(currentUserId);

        if (!isAdmin && !isPatient && !isDoctor) {
            throw new AccessDeniedException("No tiene permisos para consultar el historial de esta cita médica.");
        }

        List<AppointmentStatusHistoryEntity> historyList = historyRepository.findByAppointmentIdOrderByChangedAtAsc(appointmentId);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        return historyList.stream().map(h -> {
            String actorName = "Sistema Automático";
            if (h.getChangedBy() != null) {
                actorName = (h.getChangedBy().getFirstName() + " " + h.getChangedBy().getLastName()).trim();
                if (actorName.isEmpty()) {
                    actorName = h.getChangedBy().getEmail();
                }
            } else if ("USER".equalsIgnoreCase(h.getChangeSource())) {
                actorName = "Paciente";
            } else if ("ADMIN".equalsIgnoreCase(h.getChangeSource())) {
                actorName = "Administración Médica";
            }

            String changedAtStr = h.getChangedAt() != null ? h.getChangedAt().format(dtf) : "";

            return new AppointmentHistoryDto(
                h.getId(),
                appointment.getId(),
                h.getStatus() != null ? h.getStatus().getCode() : "UNKNOWN",
                h.getStatus() != null ? h.getStatus().getName() : "Desconocido",
                h.getChangeSource(),
                actorName,
                h.getReason(),
                changedAtStr
            );
        }).toList();
    }
}
