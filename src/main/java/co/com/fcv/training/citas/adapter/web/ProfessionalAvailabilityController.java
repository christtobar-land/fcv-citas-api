package co.com.fcv.training.citas.adapter.web;

import co.com.fcv.training.citas.application.Ports;
import co.com.fcv.training.citas.application.SchedulingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/professional/availability-blocks")
@PreAuthorize("hasRole('PROFESSIONAL')")
class ProfessionalAvailabilityController {
    record BlockRequest(@NotNull Short locationId, @NotNull LocalDate date, @NotNull LocalTime startTime, @NotNull LocalTime endTime) {}
    private final SchedulingService scheduling;
    ProfessionalAvailabilityController(SchedulingService scheduling) { this.scheduling = scheduling; }

    @GetMapping
    List<Ports.AvailabilityBlockView> list(@AuthenticationPrincipal Jwt jwt,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                           @RequestParam(required = false) Short locationId) {
        return scheduling.blocks(userId(jwt), date, locationId);
    }
    @PostMapping
    ResponseEntity<Ports.AvailabilityBlockView> create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody BlockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduling.create(userId(jwt), block(request)));
    }
    @PatchMapping("/{id}")
    Ports.AvailabilityBlockView update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @Valid @RequestBody BlockRequest request) {
        return scheduling.update(userId(jwt), id, block(request));
    }
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        scheduling.delete(userId(jwt), id); return ResponseEntity.noContent().build();
    }
    private SchedulingService.Block block(BlockRequest request) {
        return new SchedulingService.Block(request.locationId(), request.date(), request.startTime(), request.endTime());
    }
    private Long userId(Jwt jwt) { return Long.valueOf(jwt.getSubject()); }
}
