package co.fcv.citas.adapter.web;

import co.fcv.citas.application.SchedulingService;
import co.fcv.citas.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AppointmentController {
    private final SchedulingFacade facade;

    public AppointmentController(SchedulingFacade facade) {
        this.facade = facade;
    }

    public record BookRequest(
            @NotNull Long professionalId,
            @NotNull Short locationId,
            @NotNull Short specialtyId,
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt
    ) {}

    public record RejectRequest(@NotBlank String reason) {}
    public record NoShowRequest(String reason) {}

    public record AppointmentResponse(
            Long id,
            Long patientUserId,
            Long professionalId,
            String professionalName,
            Short locationId,
            String locationName,
            Short specialtyId,
            String specialtyName,
            Short statusId,
            String statusCode,
            String statusName,
            LocalDateTime scheduledStartAt,
            LocalDateTime scheduledEndAt,
            String rejectionReason,
            Instant createdAt
    ) {}

    private AppointmentResponse toResponse(Appointment a) {
        Map<Short, Location> locMap = facade.getLocations().stream().collect(Collectors.toMap(Location::id, Function.identity()));
        Map<Short, Specialty> specMap = facade.getSpecialties().stream().collect(Collectors.toMap(Specialty::id, Function.identity()));
        Map<Short, AppointmentStatus> statMap = facade.getAppointmentStatuses().stream().collect(Collectors.toMap(AppointmentStatus::id, Function.identity()));

        String profName = "Profesional";
        try {
            Professional p = facade.getProfessional(a.professionalId());
            profName = "Dr(a). " + p.firstName() + " " + p.lastName();
        } catch (Exception ignored) {}

        Location loc = locMap.get(a.locationId());
        Specialty spec = specMap.get(a.specialtyId());
        AppointmentStatus stat = statMap.get(a.statusId());

        return new AppointmentResponse(
                a.id(),
                a.patientUserId(),
                a.professionalId(),
                profName,
                a.locationId(),
                loc != null ? loc.name() : "Sede " + a.locationId(),
                a.specialtyId(),
                spec != null ? spec.name() : "Especialidad " + a.specialtyId(),
                a.statusId(),
                stat != null ? stat.code() : "UNKNOWN",
                stat != null ? stat.name() : "Estado " + a.statusId(),
                a.scheduledStartAt(),
                a.scheduledEndAt(),
                a.rejectionReason(),
                a.createdAt()
        );
    }

    // --- Paciente / General Citas ---

    @PostMapping("/appointments")
    public ResponseEntity<AppointmentResponse> book(
            @Valid @RequestBody BookRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        Appointment app = facade.bookAppointment(new SchedulingService.BookAppointmentCommand(
                userId, req.professionalId(), req.locationId(), req.specialtyId(), req.startAt()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(app));
    }

    @GetMapping("/appointments/my-appointments")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        List<Appointment> list = facade.getMyAppointments(userId);
        return ResponseEntity.ok(list.stream().map(this::toResponse).toList());
    }

    @PatchMapping("/appointments/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            Authentication auth
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Appointment app = facade.cancelAppointment(id, userId, isAdmin);
        return ResponseEntity.ok(toResponse(app));
    }

    @GetMapping("/appointments/{id}/history")
    public ResponseEntity<List<AppointmentStatusHistory>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(facade.getAppointmentHistory(id));
    }

    // --- Administración ---

    @GetMapping("/admin/appointments")
    public ResponseEntity<List<AppointmentResponse>> getAdminAppointments(
            @RequestParam(required = false) Short statusId,
            @RequestParam(required = false) Short locationId,
            @RequestParam(required = false) Long professionalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<Appointment> list = facade.getAdminAppointments(statusId, locationId, professionalId, date);
        return ResponseEntity.ok(list.stream().map(this::toResponse).toList());
    }

    @PatchMapping("/admin/appointments/{id}/approve")
    public ResponseEntity<AppointmentResponse> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long adminUserId = Long.valueOf(jwt.getSubject());
        Appointment app = facade.approveAppointment(id, adminUserId);
        return ResponseEntity.ok(toResponse(app));
    }

    @PatchMapping("/admin/appointments/{id}/reject")
    public ResponseEntity<AppointmentResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long adminUserId = Long.valueOf(jwt.getSubject());
        Appointment app = facade.rejectAppointment(id, adminUserId, req.reason());
        return ResponseEntity.ok(toResponse(app));
    }

    // --- Profesional ---

    @GetMapping("/professional/appointments")
    public ResponseEntity<List<AppointmentResponse>> getProfessionalAppointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long professionalUserId = Long.valueOf(jwt.getSubject());
        List<Appointment> list = facade.getProfessionalAppointments(professionalUserId, date);
        return ResponseEntity.ok(list.stream().map(this::toResponse).toList());
    }

    @PatchMapping("/professional/appointments/{id}/complete")
    public ResponseEntity<AppointmentResponse> complete(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long professionalUserId = Long.valueOf(jwt.getSubject());
        Appointment app = facade.completeAppointment(id, professionalUserId);
        return ResponseEntity.ok(toResponse(app));
    }

    @PatchMapping("/professional/appointments/{id}/no-show")
    public ResponseEntity<AppointmentResponse> noShow(
            @PathVariable Long id,
            @RequestBody(required = false) NoShowRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long professionalUserId = Long.valueOf(jwt.getSubject());
        Appointment app = facade.noShowAppointment(id, professionalUserId, req != null ? req.reason() : null);
        return ResponseEntity.ok(toResponse(app));
    }
}
