package co.fcv.citas.adapter.web;

import co.fcv.citas.domain.AppointmentReminder;
import co.fcv.citas.domain.NotificationEvent;
import co.fcv.citas.domain.UpcomingAppointmentView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/integrations")
public class IntegrationController {
    private final SchedulingFacade facade;
    private final String expectedKey;

    public IntegrationController(
            SchedulingFacade facade,
            @Value("${app.integration-key:fcv-n8n-integration-secret-key-2026}") String expectedKey
    ) {
        this.facade = facade;
        this.expectedKey = expectedKey;
    }

    private boolean isAuthorized(String headerKey, Authentication auth) {
        if (headerKey != null && headerKey.equals(expectedKey)) {
            return true;
        }
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @GetMapping("/appointments/upcoming")
    public ResponseEntity<?> getUpcomingAppointments(
            @RequestParam(defaultValue = "48") int hoursAhead,
            @RequestHeader(value = "X-Integration-Key", required = false) String key,
            Authentication auth
    ) {
        if (!isAuthorized(key, auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Clave de integración inválida o faltante."));
        }
        List<UpcomingAppointmentView> list = facade.getUpcomingAppointments(hoursAhead);
        return ResponseEntity.ok(list);
    }

    public record RecordReminderRequest(@NotNull Long appointmentId, String channel) {}

    @PostMapping("/appointments/reminders")
    public ResponseEntity<?> recordReminder(
            @Valid @RequestBody RecordReminderRequest req,
            @RequestHeader(value = "X-Integration-Key", required = false) String key,
            Authentication auth
    ) {
        if (!isAuthorized(key, auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Clave de integración inválida o faltante."));
        }
        AppointmentReminder reminder = facade.recordReminder(req.appointmentId(), req.channel());
        return ResponseEntity.status(HttpStatus.CREATED).body(reminder);
    }

    @GetMapping("/events/pending")
    public ResponseEntity<?> getPendingEvents(
            @RequestHeader(value = "X-Integration-Key", required = false) String key,
            Authentication auth
    ) {
        if (!isAuthorized(key, auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Clave de integración inválida o faltante."));
        }
        List<NotificationEvent> events = facade.getPendingEvents();
        return ResponseEntity.ok(events);
    }

    @PostMapping("/events/{id}/ack")
    public ResponseEntity<?> acknowledgeEvent(
            @PathVariable Long id,
            @RequestHeader(value = "X-Integration-Key", required = false) String key,
            Authentication auth
    ) {
        if (!isAuthorized(key, auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Clave de integración inválida o faltante."));
        }
        facade.markEventProcessed(id);
        return ResponseEntity.ok(Map.of("status", "PROCESSED", "eventId", id));
    }

    @GetMapping("/reports/daily-summary")
    public ResponseEntity<?> getDailySummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader(value = "X-Integration-Key", required = false) String key,
            Authentication auth
    ) {
        if (!isAuthorized(key, auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Clave de integración inválida o faltante."));
        }
        Map<String, Object> summary = facade.getDailyOperationalSummary(date);
        return ResponseEntity.ok(summary);
    }
}
