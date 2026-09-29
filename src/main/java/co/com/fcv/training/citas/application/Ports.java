package co.com.fcv.training.citas.application;

import co.com.fcv.training.citas.domain.Account;
import co.com.fcv.training.citas.domain.RefreshSession;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public final class Ports {
    private Ports() {}

    public interface Accounts {
        boolean existsEmail(String email);
        boolean existsDocument(String type, String number);
        Optional<Account> byEmail(String email);
        Optional<Account> byId(Long id);
        Account save(Account account);
    }

    public interface Affiliations {
        Optional<InsurancePlan> activePlan(Long planId);
        void createInitial(Long userId, Long planId);
    }

    public interface Offer {
        List<SpecialtyView> specialties();
        List<SpecialtyView> activeSpecialties();
        SpecialtyView createSpecialty(String code, String name, short durationMinutes, boolean general,
                                      boolean requiresAdminApproval);
        SpecialtyView patchSpecialty(Short id, String name, Short durationMinutes, Boolean general,
                                     Boolean requiresAdminApproval, Boolean active);
        ProfessionalView createProfessional(Long userId, String professionalCode, String licenseNumber);
        void replaceSpecialties(Long professionalId, List<Short> specialtyIds, Short primarySpecialtyId);
        void replaceLocations(Long professionalId, List<Short> locationIds);
        void setProfessionalActive(Long professionalId, boolean active);
    }

    public interface Scheduling {
        AvailabilityBlockView createBlock(Long professionalUserId, Short locationId, LocalDate date,
                                          LocalTime startTime, LocalTime endTime);
        List<AvailabilityBlockView> blocks(Long professionalUserId, LocalDate date, Short locationId);
        AvailabilityBlockView updateBlock(Long professionalUserId, Long blockId, Short locationId,
                                          LocalDate date, LocalTime startTime, LocalTime endTime);
        void deleteBlock(Long professionalUserId, Long blockId);
        List<AvailabilityOption> availability(Short locationId, Short specialtyId, Long professionalId, LocalDate date);
        AppointmentView reserve(Long patientUserId, Long professionalId, Short locationId, Short specialtyId,
                                LocalDateTime startAt, String reason);
        List<AppointmentView> requestedAppointments();
        AppointmentView decide(Long adminUserId, Long appointmentId, String decision, String reason);
        List<AppointmentView> patientAppointments(Long patientUserId, String status, LocalDate date);
        AppointmentView cancel(Long patientUserId, Long appointmentId);
        List<AppointmentView> professionalAppointments(Long professionalUserId, LocalDate date, Short locationId);
        AppointmentView close(Long professionalUserId, Long appointmentId, String outcome);
        List<AppointmentHistoryView> history(Long actorUserId, java.util.Set<String> roles, Long appointmentId);
    }

    public interface Sessions {
        void save(RefreshSession session);
        Optional<RefreshSession> lockByJtiHash(String hash);
        void revoke(Long id, Instant when);
    }

    public interface Passwords {
        String hash(String raw);
        boolean matches(String raw, String hash);
    }

    public record IssuedRefresh(String value, String jti, Instant expiresAt) {}
    public record RefreshIdentity(Long userId, String jti) {}

    public interface Tokens {
        String access(Long userId, Set<String> roles);
        IssuedRefresh refresh(Long userId);
        RefreshIdentity readRefresh(String token);
        long accessSeconds();
    }

    public interface Transactions {
        <T> T run(Supplier<T> work);
    }

    public interface Catalogs {
        List<RoleCatalog> roles();
        List<StatusCatalog> appointmentStatuses();
        List<StatusCatalog> rescheduleStatuses();
        List<RegimeCatalog> insuranceRegimes();
        List<LocationCatalog> locations();
        List<InsurancePlanCatalog> insurancePlans();
    }

    public record RoleCatalog(short id, String code, String name, String description) {}
    public record StatusCatalog(short id, String code, String name, boolean terminal) {}
    public record RegimeCatalog(short id, String code, String name) {}
    public record LocationCatalog(short id, String code, String name, String address,
                                  String city, String department, boolean active) {}
    public record InsurancePlanCatalog(Long id, Long epsId, String epsCode, String epsName,
                                      short regimeId, String regimeCode, String regimeName,
                                      String code, String name) {}
    public record InsurancePlan(Long id) {}
    public record SpecialtyView(short id, String code, String name, short appointmentDurationMinutes,
                                boolean general, boolean requiresAdminApproval, boolean active) {}
    public record ProfessionalView(Long id, Long userId, String professionalCode, String licenseNumber,
                                   boolean active) {}
    public record AvailabilityBlockView(Long id, Long professionalId, short locationId, LocalDate date,
                                        LocalTime startTime, LocalTime endTime) {}
    public record AvailabilityOption(Long professionalId, short locationId, short specialtyId,
                                     LocalDateTime startAt, LocalDateTime endAt, short durationMinutes,
                                     boolean general, String professionalName, String locationName, String specialtyName) {}
    public record AppointmentView(Long id, Long patientUserId, Long professionalId, short locationId,
                                  short specialtyId, String status, LocalDateTime startAt, LocalDateTime endAt,
                                  String reason, String locationName, String specialtyName, String professionalName,
                                  String patientName) {}
    public record AppointmentHistoryView(Long appointmentId, String status, Long actorUserId, String source,
                                         String reason, java.time.Instant changedAt) {}
}
