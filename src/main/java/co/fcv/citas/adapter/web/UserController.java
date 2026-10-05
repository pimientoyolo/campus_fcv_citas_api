package co.fcv.citas.adapter.web;

import co.fcv.citas.application.AuthService;
import co.fcv.citas.application.SchedulingService;
import co.fcv.citas.domain.User;
import co.fcv.citas.domain.UserAffiliation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final AuthFacade authFacade;
    private final SchedulingFacade schedulingFacade;

    public UserController(AuthFacade authFacade, SchedulingFacade schedulingFacade) {
        this.authFacade = authFacade;
        this.schedulingFacade = schedulingFacade;
    }

    public record UserProfileResponse(
            Long id,
            String firstName,
            String lastName,
            String documentType,
            String documentNumber,
            String email,
            String phone,
            List<UserAffiliation> affiliations
    ) {
        static UserProfileResponse of(User u, List<UserAffiliation> affs) {
            return new UserProfileResponse(u.id(), u.firstName(), u.lastName(), u.documentType(),
                    u.documentNumber(), u.email(), u.phone(), affs);
        }
    }

    public record UpdateProfileRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank String phone
    ) {}

    public record CreateAffiliationRequest(
            @NotNull Short epsId,
            @NotNull Short epsPlanId,
            @NotNull Short regimenId
    ) {}

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        User u = authFacade.getUserProfile(userId);
        List<UserAffiliation> affs = schedulingFacade.getUserAffiliations(userId);
        return ResponseEntity.ok(UserProfileResponse.of(u, affs));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProfileRequest req
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        User updated = authFacade.updateProfile(userId, new AuthService.UpdateProfileCommand(req.firstName(), req.lastName(), req.phone()));
        List<UserAffiliation> affs = schedulingFacade.getUserAffiliations(userId);
        return ResponseEntity.ok(UserProfileResponse.of(updated, affs));
    }

    @GetMapping("/affiliations")
    public ResponseEntity<List<UserAffiliation>> listAffiliations(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(schedulingFacade.getUserAffiliations(userId));
    }

    @PostMapping("/affiliations")
    public ResponseEntity<UserAffiliation> createAffiliation(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateAffiliationRequest req
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        UserAffiliation aff = schedulingFacade.createAffiliation(
                new SchedulingService.CreateAffiliationCommand(userId, req.epsId(), req.epsPlanId(), req.regimenId())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(aff);
    }

    @DeleteMapping("/affiliations/{id}")
    public ResponseEntity<Void> deactivateAffiliation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        schedulingFacade.deactivateAffiliation(id);
        return ResponseEntity.noContent().build();
    }
}
