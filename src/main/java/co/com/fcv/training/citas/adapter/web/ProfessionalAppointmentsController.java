package co.com.fcv.training.citas.adapter.web;

import co.com.fcv.training.citas.application.Ports;
import co.com.fcv.training.citas.application.SchedulingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/professional/appointments")
@PreAuthorize("hasRole('PROFESSIONAL')")
class ProfessionalAppointmentsController {
    record CloseRequest(@NotBlank @jakarta.validation.constraints.Pattern(regexp = "COMPLETED|NO_SHOW") String outcome) {}
    private final SchedulingService scheduling;
    ProfessionalAppointmentsController(SchedulingService scheduling) { this.scheduling = scheduling; }
    @GetMapping List<Ports.AppointmentView> agenda(@AuthenticationPrincipal Jwt jwt, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @RequestParam(required = false) Short locationId) {
        return scheduling.professionalAgenda(Long.valueOf(jwt.getSubject()), date, locationId);
    }
    @PostMapping("/{id}/close") Ports.AppointmentView close(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @Valid @RequestBody CloseRequest request) {
        return scheduling.close(Long.valueOf(jwt.getSubject()), id, request.outcome());
    }
}
