package co.com.fcv.training.citas.adapter.web;

import co.com.fcv.training.citas.application.ProfessionalService;
import co.com.fcv.training.citas.application.SpecialtyService;
import co.com.fcv.training.citas.application.Ports;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminOfferController {
    record SpecialtyRequest(@NotBlank @Size(max = 50) String code,
                            @NotBlank @Size(max = 150) String name,
                            @Min(30) @Max(60) short appointmentDurationMinutes,
                            boolean general, boolean requiresAdminApproval) {}
    record SpecialtyPatch(@Size(max = 150) String name, @Min(30) @Max(60) Short appointmentDurationMinutes,
                          Boolean general, Boolean requiresAdminApproval, Boolean active) {}
    record ProfessionalRequest(@NotBlank @Size(max = 120) String firstName,
                               @NotBlank @Size(max = 120) String lastName,
                               @NotBlank @Size(max = 30) String documentType,
                               @NotBlank @Size(max = 80) String documentNumber,
                               @NotBlank @Size(max = 254) String email,
                               @NotBlank @Size(max = 40) String phone,
                               @NotBlank String temporaryPassword,
                               @NotBlank @Size(max = 40) String professionalCode,
                               @NotBlank @Size(max = 80) String licenseNumber) {}
    record SpecialtyAssignment(@NotEmpty List<@NotNull Short> specialtyIds,
                               @NotNull Short primarySpecialtyId) {}
    record LocationAssignment(@NotEmpty @Size(max = 2) List<@NotNull Short> locationIds) {}
    record ActiveRequest(boolean active) {}

    private final SpecialtyService specialties;
    private final ProfessionalService professionals;

    AdminOfferController(SpecialtyService specialties, ProfessionalService professionals) {
        this.specialties = specialties; this.professionals = professionals;
    }

    @GetMapping("/specialties")
    List<Ports.SpecialtyView> specialties() { return specialties.all(); }

    @PostMapping("/specialties")
    ResponseEntity<Ports.SpecialtyView> createSpecialty(@Valid @RequestBody SpecialtyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(specialties.create(new SpecialtyService.Create(
                request.code(), request.name(), request.appointmentDurationMinutes(), request.general(),
                request.requiresAdminApproval())));
    }

    @PatchMapping("/specialties/{id}")
    Ports.SpecialtyView patchSpecialty(@PathVariable Short id, @Valid @RequestBody SpecialtyPatch request) {
        return specialties.patch(id, new SpecialtyService.Patch(request.name(), request.appointmentDurationMinutes(),
                request.general(), request.requiresAdminApproval(), request.active()));
    }

    @PostMapping("/professionals")
    ResponseEntity<Ports.ProfessionalView> createProfessional(@Valid @RequestBody ProfessionalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(professionals.create(new ProfessionalService.Create(
                request.firstName(), request.lastName(), request.documentType(), request.documentNumber(),
                request.email(), request.phone(), request.temporaryPassword(), request.professionalCode(),
                request.licenseNumber())));
    }

    @PutMapping("/professionals/{id}/specialties")
    ResponseEntity<Void> assignSpecialties(@PathVariable Long id, @Valid @RequestBody SpecialtyAssignment request) {
        professionals.assignSpecialties(id, request.specialtyIds(), request.primarySpecialtyId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/professionals/{id}/locations")
    ResponseEntity<Void> assignLocations(@PathVariable Long id, @Valid @RequestBody LocationAssignment request) {
        professionals.assignLocations(id, request.locationIds());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/professionals/{id}/active")
    ResponseEntity<Void> setActive(@PathVariable Long id, @Valid @RequestBody ActiveRequest request) {
        professionals.setActive(id, request.active());
        return ResponseEntity.noContent().build();
    }
}
