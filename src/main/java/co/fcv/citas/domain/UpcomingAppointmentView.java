package co.fcv.citas.domain;

import java.time.LocalDateTime;

public record UpcomingAppointmentView(
        Long appointmentId,
        Long patientUserId,
        String patientName,
        String patientEmail,
        String patientPhone,
        Long professionalId,
        String professionalName,
        String specialtyName,
        String locationName,
        LocalDateTime scheduledStartAt,
        LocalDateTime scheduledEndAt,
        String status
) {}
