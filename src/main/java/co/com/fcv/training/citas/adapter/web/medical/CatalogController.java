package co.com.fcv.training.citas.adapter.web.medical;

import co.com.fcv.training.citas.application.medical.MedicalServices;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {

    private final MedicalServices services;

    public CatalogController(MedicalServices services) {
        this.services = services;
    }

    @GetMapping("/locations")
    public ResponseEntity<List<MedicalServices.LocationDto>> getLocations() {
        return ResponseEntity.ok(services.getLocations());
    }

    @GetMapping("/specialties")
    public ResponseEntity<List<MedicalServices.SpecialtyDto>> getSpecialties() {
        return ResponseEntity.ok(services.getSpecialties());
    }

    @GetMapping("/eps")
    public ResponseEntity<List<MedicalServices.EpsDto>> getEps() {
        return ResponseEntity.ok(services.getEpsList());
    }

    @GetMapping("/professionals")
    public ResponseEntity<List<MedicalServices.ProfessionalDto>> getProfessionals(
        @RequestParam(required = false) Short specialtyId,
        @RequestParam(required = false) Short locationId
    ) {
        return ResponseEntity.ok(services.getProfessionals(specialtyId, locationId));
    }

    @GetMapping("/my-affiliation")
    public ResponseEntity<MedicalServices.UserAffiliationDto> getMyAffiliation(@AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt != null ? Long.valueOf(jwt.getSubject()) : 100L;
        return ResponseEntity.ok(services.getUserAffiliation(userId));
    }
}
