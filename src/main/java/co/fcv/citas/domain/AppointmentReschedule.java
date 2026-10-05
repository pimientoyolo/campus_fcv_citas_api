package co.fcv.citas.domain;

import java.time.Instant;
import java.time.LocalDateTime;

public record AppointmentReschedule(
        Long id,
        Long appointmentId,
        Long requestedByUserId,
        LocalDateTime oldStartAt,
        LocalDateTime newStartAt,
        LocalDateTime newEndAt,
        Short statusId,
        String statusCode,
        String reason,
        String rejectionReason,
        Instant createdAt,
        Instant updatedAt
) {}
