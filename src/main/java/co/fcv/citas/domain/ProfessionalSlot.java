package co.fcv.citas.domain;

import java.time.LocalDateTime;

public record ProfessionalSlot(Long id, Long availabilityBlockId, Long professionalId, Long appointmentId,
                               LocalDateTime startAt, LocalDateTime endAt) {
    public boolean isAvailable() {
        return appointmentId == null;
    }
}
