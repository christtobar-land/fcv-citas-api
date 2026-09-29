package co.com.fcv.training.citas.adapter.web;

import co.com.fcv.training.citas.application.Ports;
import co.com.fcv.training.citas.application.SchedulingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/appointments")
@PreAuthorize("hasRole('ADMIN')")
class AdminAppointmentController {
    record DecisionRequest(@NotBlank @Pattern(regexp = "APPROVE|REJECT") String decision, @Size(max = 500) String reason) {}
    private final SchedulingService scheduling;
    AdminAppointmentController(SchedulingService scheduling) { this.scheduling = scheduling; }
    @GetMapping
    List<Ports.AppointmentView> requested() { return scheduling.requested(); }
    @PostMapping("/{id}/decision")
    Ports.AppointmentView decide(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @Valid @RequestBody DecisionRequest request) {
        return scheduling.decide(Long.valueOf(jwt.getSubject()), id, request.decision(), request.reason());
    }
}
