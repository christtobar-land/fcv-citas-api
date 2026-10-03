package co.com.fcv.training.citas.adapter.web;

import co.com.fcv.training.citas.adapter.persistence.RoleEntity;
import co.com.fcv.training.citas.adapter.persistence.UserEntity;
import co.com.fcv.training.citas.adapter.persistence.UsersJpa;
import co.com.fcv.training.citas.adapter.persistence.medical.UserInsuranceAffiliationEntity;
import co.com.fcv.training.citas.adapter.persistence.medical.UserInsuranceAffiliationRepository;
import co.com.fcv.training.citas.application.DuplicateIdentity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    public record AffiliationDto(
        Long epsId,
        String epsName,
        Long planId,
        String planName,
        Short regimeId,
        String regimeName,
        String membershipNumber
    ) {}

    public record UserProfileDto(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String documentType,
        String documentNumber,
        String email,
        String phone,
        List<String> roles,
        AffiliationDto affiliation
    ) {}

    public record UpdateProfileRequest(
        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "Formato de correo electrónico inválido")
        @Size(max = 160, message = "El correo no puede exceder 160 caracteres")
        String email,

        @NotBlank(message = "El número telefónico es obligatorio")
        @Pattern(regexp = "^3\\d{9}$", message = "El teléfono debe ser un celular válido de 10 dígitos (ej. 3001234567)")
        String phone
    ) {}

    private final UsersJpa usersJpa;
    private final UserInsuranceAffiliationRepository affiliationRepository;

    public UserController(UsersJpa usersJpa, UserInsuranceAffiliationRepository affiliationRepository) {
        this.usersJpa = usersJpa;
        this.affiliationRepository = affiliationRepository;
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public ResponseEntity<UserProfileDto> getProfile(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        UserEntity user = usersJpa.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return ResponseEntity.ok(toDto(user));
    }

    @PatchMapping("/me/profile")
    @Transactional
    public ResponseEntity<UserProfileDto> updateProfile(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        UserEntity user = usersJpa.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        String newEmail = request.email().trim().toLowerCase();
        String newPhone = request.phone().trim();

        if (!user.getEmail().equalsIgnoreCase(newEmail)) {
            if (usersJpa.existsByEmail(newEmail)) {
                throw new DuplicateIdentity();
            }
            user.setEmail(newEmail);
        }

        user.setPhone(newPhone);
        UserEntity saved = usersJpa.save(user);

        return ResponseEntity.ok(toDto(saved));
    }

    private UserProfileDto toDto(UserEntity user) {
        List<String> roles = user.getRoles().stream()
            .map(RoleEntity::getCode)
            .toList();

        AffiliationDto affiliationDto = affiliationRepository.findByUserIdAndIsCurrentTrue(user.getId())
            .map(aff -> {
                String epsName = aff.getPlan() != null && aff.getPlan().getEps() != null 
                    ? aff.getPlan().getEps().getName() : null;
                Long epsId = aff.getPlan() != null && aff.getPlan().getEps() != null 
                    ? aff.getPlan().getEps().getId() : null;
                String planName = aff.getPlan() != null ? aff.getPlan().getName() : null;
                Long planId = aff.getPlan() != null ? aff.getPlan().getId() : null;
                Short regimeId = aff.getPlan() != null ? aff.getPlan().getRegimeId() : null;
                String regimeName = getRegimeName(regimeId);
                return new AffiliationDto(epsId, epsName, planId, planName, regimeId, regimeName, aff.getMembershipNumber());
            })
            .orElse(null);

        return new UserProfileDto(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            (user.getFirstName() + " " + user.getLastName()).trim(),
            user.getDocumentType(),
            user.getDocumentNumber(),
            user.getEmail(),
            user.getPhone(),
            roles,
            affiliationDto
        );
    }

    private static String getRegimeName(Short regimeId) {
        if (regimeId == null) return "No especificado";
        return switch (regimeId) {
            case 1 -> "Contributivo";
            case 2 -> "Subsidiado";
            case 3 -> "Especial";
            case 4 -> "Excepción";
            case 5 -> "Particular";
            default -> "Régimen General";
        };
    }
}
