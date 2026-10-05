package co.fcv.citas.adapter.web;

import co.fcv.citas.domain.Eps;
import co.fcv.citas.domain.EpsPlan;
import co.fcv.citas.domain.Specialty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/catalogs")
public class AdminCatalogController {
    private final SchedulingFacade facade;

    public AdminCatalogController(SchedulingFacade facade) {
        this.facade = facade;
    }

    public record CreateEpsRequest(@NotBlank String code, @NotBlank String name) {}
    public record UpdateEpsRequest(String code, String name, Boolean active) {}
    public record ToggleActiveRequest(@NotNull Boolean active) {}

    @PostMapping("/eps")
    public ResponseEntity<Eps> createEps(@Valid @RequestBody CreateEpsRequest req) {
        Eps created = facade.saveEps(new Eps(null, req.code().strip(), req.name().strip(), true));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/eps/{id}")
    public ResponseEntity<Eps> updateEps(@PathVariable Short id, @RequestBody UpdateEpsRequest req) {
        Eps existing = facade.getEpsList().stream().filter(e -> e.id().equals(id)).findFirst().orElse(null);
        if (existing == null) return ResponseEntity.notFound().build();
        Eps updated = facade.saveEps(new Eps(
                id,
                req.code() != null ? req.code().strip() : existing.code(),
                req.name() != null ? req.name().strip() : existing.name(),
                req.active() != null ? req.active() : existing.active()
        ));
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/eps/{id}/active")
    public ResponseEntity<Eps> toggleEpsActive(@PathVariable Short id, @Valid @RequestBody ToggleActiveRequest req) {
        Eps existing = facade.getEpsList().stream().filter(e -> e.id().equals(id)).findFirst().orElse(null);
        if (existing == null) return ResponseEntity.notFound().build();
        Eps updated = facade.saveEps(new Eps(id, existing.code(), existing.name(), req.active()));
        return ResponseEntity.ok(updated);
    }

    public record CreatePlanRequest(@NotBlank String code, @NotBlank String name) {}
    public record UpdatePlanRequest(String code, String name, Boolean active) {}

    @PostMapping("/eps/{epsId}/plans")
    public ResponseEntity<EpsPlan> createPlan(@PathVariable Short epsId, @Valid @RequestBody CreatePlanRequest req) {
        EpsPlan created = facade.saveEpsPlan(new EpsPlan(null, epsId, req.code().strip(), req.name().strip(), true));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/eps-plans/{id}")
    public ResponseEntity<EpsPlan> updatePlan(@PathVariable Short id, @RequestBody UpdatePlanRequest req) {
        EpsPlan updated = facade.saveEpsPlan(new EpsPlan(
                id, (short) 1, req.code(), req.name(), req.active() != null ? req.active() : true
        ));
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/eps-plans/{id}/active")
    public ResponseEntity<EpsPlan> togglePlanActive(@PathVariable Short id, @Valid @RequestBody ToggleActiveRequest req) {
        EpsPlan updated = facade.saveEpsPlan(new EpsPlan(
                id, (short) 1, null, null, req.active()
        ));
        return ResponseEntity.ok(updated);
    }

    public record CreateSpecialtyRequest(
            @NotBlank String code,
            @NotBlank String name,
            @NotNull Integer appointmentDurationMinutes,
            Boolean isGeneral,
            Boolean requiresAdminApproval
    ) {}

    @PostMapping("/specialties")
    public ResponseEntity<Specialty> createSpecialty(@Valid @RequestBody CreateSpecialtyRequest req) {
        Specialty created = facade.saveSpecialty(new Specialty(
                null,
                req.code().strip(),
                req.name().strip(),
                req.appointmentDurationMinutes(),
                req.isGeneral() != null ? req.isGeneral() : false,
                req.requiresAdminApproval() != null ? req.requiresAdminApproval() : true,
                true
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/specialties/{id}")
    public ResponseEntity<Specialty> updateSpecialty(@PathVariable Short id, @RequestBody Specialty req) {
        Specialty updated = facade.saveSpecialty(new Specialty(
                id,
                req.code(),
                req.name(),
                req.durationMinutes(),
                req.isGeneral(),
                req.requiresAdminApproval(),
                req.active()
        ));
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/specialties/{id}/active")
    public ResponseEntity<Specialty> toggleSpecialtyActive(@PathVariable Short id, @Valid @RequestBody ToggleActiveRequest req) {
        Specialty existing = facade.getSpecialties().stream().filter(s -> s.id().equals(id)).findFirst().orElse(null);
        if (existing == null) return ResponseEntity.notFound().build();
        Specialty updated = facade.saveSpecialty(new Specialty(
                id, existing.code(), existing.name(), existing.durationMinutes(),
                existing.isGeneral(), existing.requiresAdminApproval(), req.active()
        ));
        return ResponseEntity.ok(updated);
    }
}
