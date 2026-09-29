package co.com.fcv.training.citas.adapter.web;

import co.com.fcv.training.citas.application.Ports;
import co.com.fcv.training.citas.application.SchedulingService;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/availability")
@PreAuthorize("hasRole('USER')")
class AvailabilityController {
    private final SchedulingService scheduling;
    AvailabilityController(SchedulingService scheduling) { this.scheduling = scheduling; }
    @GetMapping
    List<Ports.AvailabilityOption> search(@RequestParam @NotNull Short locationId, @RequestParam @NotNull Short specialtyId,
                                          @RequestParam(required = false) Long professionalId,
                                          @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return scheduling.search(locationId, specialtyId, professionalId, date);
    }
}
