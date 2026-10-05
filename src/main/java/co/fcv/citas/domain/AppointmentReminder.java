package co.fcv.citas.domain;

import java.time.Instant;

public record AppointmentReminder(
        Long id,
        Long appointmentId,
        Instant scheduledFor,
        Instant sentAt,
        String status,
        String channel
) {}
