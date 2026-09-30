package co.fcv.citas.domain;

import java.time.LocalDate;
import java.time.LocalTime;

public record AvailabilityBlock(Long id, Long professionalId, Short locationId,
                                LocalDate availableDate, LocalTime startTime, LocalTime endTime,
                                boolean active) {
}
