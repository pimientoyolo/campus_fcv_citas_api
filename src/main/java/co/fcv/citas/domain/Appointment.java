package co.fcv.citas.domain;

import java.time.Instant;
import java.time.LocalDateTime;

public record Appointment(Long id, Long patientUserId, Long professionalId, Short locationId,
                          Short specialtyId, Short statusId, LocalDateTime scheduledStartAt,
                          LocalDateTime scheduledEndAt, String rejectionReason,
                          Instant createdAt, Instant updatedAt) {
}
