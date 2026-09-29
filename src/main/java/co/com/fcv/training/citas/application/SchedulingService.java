package co.com.fcv.training.citas.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class SchedulingService {
    public record Block(Short locationId, LocalDate date, LocalTime startTime, LocalTime endTime) {}
    public record Reservation(Long professionalId, Short locationId, Short specialtyId, LocalDateTime startAt, String reason) {}
    private final Ports.Scheduling scheduling;

    public SchedulingService(Ports.Scheduling scheduling) { this.scheduling = scheduling; }
    public Ports.AvailabilityBlockView create(Long userId, Block block) {
        return scheduling.createBlock(userId, block.locationId(), block.date(), block.startTime(), block.endTime());
    }
    public List<Ports.AvailabilityBlockView> blocks(Long userId, LocalDate date, Short locationId) {
        return scheduling.blocks(userId, date, locationId);
    }
    public Ports.AvailabilityBlockView update(Long userId, Long id, Block block) {
        return scheduling.updateBlock(userId, id, block.locationId(), block.date(), block.startTime(), block.endTime());
    }
    public void delete(Long userId, Long id) { scheduling.deleteBlock(userId, id); }
    public List<Ports.AvailabilityOption> search(Short locationId, Short specialtyId, Long professionalId, LocalDate date) {
        return scheduling.availability(locationId, specialtyId, professionalId, date);
    }
    public Ports.AppointmentView reserve(Long userId, Reservation reservation) {
        return scheduling.reserve(userId, reservation.professionalId(), reservation.locationId(), reservation.specialtyId(),
                reservation.startAt(), reservation.reason());
    }
    public List<Ports.AppointmentView> requested() { return scheduling.requestedAppointments(); }
    public Ports.AppointmentView decide(Long adminUserId, Long id, String decision, String reason) {
        return scheduling.decide(adminUserId, id, decision, reason);
    }
}
