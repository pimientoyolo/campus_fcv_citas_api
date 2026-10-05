package co.fcv.citas.domain;

import java.time.Instant;

public record AppointmentStatusHistory(Long id, Long appointmentId, Short statusId,
                                       Long changedByUserId, String changeSource,
                                       Instant changedAt, String reason) {
}
