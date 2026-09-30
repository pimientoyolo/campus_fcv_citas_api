package co.fcv.citas.adapter.web;

import co.fcv.citas.application.SchedulingService;
import co.fcv.citas.domain.AvailabilityBlock;
import co.fcv.citas.domain.Professional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/professionals")
public class ProfessionalController {
    private final SchedulingFacade facade;

    public ProfessionalController(SchedulingFacade facade) {
        this.facade = facade;
    }

    public record CreateBlockRequest(
            @NotNull Short locationId,
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @NotNull @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
            @NotNull @DateTimeFormat(pattern = "HH:mm") LocalTime endTime
    ) {}

    public record CreateProfessionalRequest(
            @jakarta.validation.constraints.NotBlank String firstName,
            @jakarta.validation.constraints.NotBlank String lastName,
            @jakarta.validation.constraints.NotBlank String documentType,
            @jakarta.validation.constraints.NotBlank String documentNumber,
            @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Email String email,
            @jakarta.validation.constraints.NotBlank String phone,
            @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(min = 8) String password,
            @jakarta.validation.constraints.NotBlank String professionalCode,
            @jakarta.validation.constraints.NotBlank String licenseNumber,
            @jakarta.validation.constraints.NotEmpty List<Short> specialtyIds,
            @jakarta.validation.constraints.NotEmpty List<Short> locationIds
    ) {}

    @PostMapping
    public ResponseEntity<Professional> create(
            @Valid @RequestBody CreateProfessionalRequest req,
            Authentication auth
    ) {
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Professional p = facade.createProfessional(new SchedulingService.CreateProfessionalCommand(
                req.firstName(), req.lastName(), req.documentType(), req.documentNumber(),
                req.email(), req.phone(), req.password(),
                req.professionalCode(), req.licenseNumber(),
                req.specialtyIds(), req.locationIds()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(p);
    }

    @GetMapping
    public ResponseEntity<List<Professional>> list(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Short specialtyId,
            @RequestParam(required = false) Short locationId
    ) {
        return ResponseEntity.ok(facade.listProfessionals(active, specialtyId, locationId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Professional> getById(@PathVariable Long id) {
        return ResponseEntity.ok(facade.getProfessional(id));
    }

    @GetMapping("/{id}/blocks")
    public ResponseEntity<List<AvailabilityBlock>> getBlocks(@PathVariable Long id) {
        return ResponseEntity.ok(facade.listBlocks(id));
    }

    @PostMapping("/{id}/blocks")
    public ResponseEntity<AvailabilityBlock> createBlock(
            @PathVariable Long id,
            @Valid @RequestBody CreateBlockRequest req,
            @AuthenticationPrincipal Jwt jwt,
            Authentication auth
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            Professional prof = facade.getProfessionalByUserId(userId);
            if (!prof.id().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        AvailabilityBlock block = facade.createBlock(
                new SchedulingService.CreateBlockCommand(id, req.locationId(), req.date(), req.startTime(), req.endTime())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(block);
    }

    @DeleteMapping("/{id}/blocks/{blockId}")
    public ResponseEntity<Void> deleteBlock(
            @PathVariable Long id,
            @PathVariable Long blockId,
            @AuthenticationPrincipal Jwt jwt,
            Authentication auth
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        facade.deleteBlock(blockId, userId, isAdmin);
        return ResponseEntity.noContent().build();
    }
}
