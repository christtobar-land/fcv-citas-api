package co.com.fcv.training.citas.application.automation;

import co.com.fcv.training.citas.adapter.persistence.UserEntity;
import co.com.fcv.training.citas.adapter.persistence.medical.AppointmentEntity;
import co.com.fcv.training.citas.adapter.persistence.medical.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Consultas de solo lectura para los flujos programados de n8n (WF-001 recordatorios, WF-003 resumen).
 * Devuelve el mínimo de datos necesarios y jamás modifica citas, cupos ni auditoría.
 */
@Service
public class AutomationQueries {
    public static final int MAX_WINDOW_HOURS = 168;

    private final AppointmentRepository appointments;

    public AutomationQueries(AppointmentRepository appointments) {
        this.appointments = appointments;
    }

    public record ReminderDto(Long appointmentId, String patientFirstName, String patientEmail,
                              String doctorName, String specialty, String location, String scheduledStartAt) {}

    public record SummaryRow(String location, String specialty, String status, long total) {}

    public record DailySummary(String date, long total, List<SummaryRow> rows) {}

    @Transactional(readOnly = true)
    public List<ReminderDto> upcomingApproved(int hours) {
        int window = Math.min(Math.max(hours, 1), MAX_WINDOW_HOURS);
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return appointments.findApprovedStartingBetween(now, now.plusHours(window)).stream()
                .filter(a -> a.getPatient() != null && a.getPatient().getEmail() != null)
                .map(a -> {
                    UserEntity doc = a.getProfessional().getUser();
                    return new ReminderDto(
                            a.getId(),
                            a.getPatient().getFirstName(),
                            a.getPatient().getEmail(),
                            "Dr(a). " + doc.getFirstName() + " " + doc.getLastName(),
                            a.getSpecialty().getName(),
                            a.getLocation().getName(),
                            a.getScheduledStartAt().format(dtf));
                }).toList();
    }

    @Transactional(readOnly = true)
    public DailySummary dailySummary(LocalDate date) {
        LocalDate day = date != null ? date : LocalDate.now();
        List<AppointmentEntity> list = appointments.findStartingBetween(day.atStartOfDay(), day.plusDays(1).atStartOfDay());
        Map<String, Long> grouped = list.stream().collect(Collectors.groupingBy(
                a -> a.getLocation().getName() + "\u0000" + a.getSpecialty().getName() + "\u0000" + a.getStatus().getCode(),
                TreeMap::new, Collectors.counting()));
        List<SummaryRow> rows = grouped.entrySet().stream().map(e -> {
            String[] k = e.getKey().split("\u0000");
            return new SummaryRow(k[0], k[1], k[2], e.getValue());
        }).toList();
        return new DailySummary(day.toString(), list.size(), rows);
    }
}
