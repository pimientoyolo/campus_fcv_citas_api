package co.fcv.citas.adapter.persistence;

import co.fcv.citas.application.SchedulingPorts;
import co.fcv.citas.domain.Appointment;
import co.fcv.citas.domain.AppointmentReminder;
import co.fcv.citas.domain.NotificationEvent;
import co.fcv.citas.domain.UpcomingAppointmentView;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class IntegrationPersistenceAdapter implements SchedulingPorts.Integrations {
    private final NotificationEventJpaRepository eventRepo;
    private final AppointmentReminderJpaRepository reminderRepo;
    private final AppointmentJpaRepository appointmentRepo;
    private final AppointmentStatusJpaRepository statusRepo;
    private final UserJpaRepository userRepo;
    private final ProfessionalJpaRepository profRepo;
    private final SpecialtyJpaRepository specialtyRepo;
    private final LocationJpaRepository locationRepo;

    public IntegrationPersistenceAdapter(
            NotificationEventJpaRepository eventRepo,
            AppointmentReminderJpaRepository reminderRepo,
            AppointmentJpaRepository appointmentRepo,
            AppointmentStatusJpaRepository statusRepo,
            UserJpaRepository userRepo,
            ProfessionalJpaRepository profRepo,
            SpecialtyJpaRepository specialtyRepo,
            LocationJpaRepository locationRepo
    ) {
        this.eventRepo = eventRepo;
        this.reminderRepo = reminderRepo;
        this.appointmentRepo = appointmentRepo;
        this.statusRepo = statusRepo;
        this.userRepo = userRepo;
        this.profRepo = profRepo;
        this.specialtyRepo = specialtyRepo;
        this.locationRepo = locationRepo;
    }

    @Override
    public void publishEvent(String eventType, Long aggregateId, String payload) {
        NotificationEventEntity entity = new NotificationEventEntity(
                null,
                eventType,
                aggregateId,
                payload != null ? payload : "{}",
                "PENDING",
                0,
                Instant.now(),
                null
        );
        eventRepo.save(entity);
    }

    @Override
    public List<NotificationEvent> findPendingEvents() {
        return eventRepo.findByStatusOrderByCreatedAtAsc("PENDING").stream()
                .map(this::toEvent)
                .toList();
    }

    @Override
    public void markEventProcessed(Long eventId) {
        eventRepo.findById(eventId).ifPresent(e -> {
            e.setStatus("PROCESSED");
            e.setProcessedAt(Instant.now());
            eventRepo.save(e);
        });
    }

    @Override
    public AppointmentReminder saveReminder(AppointmentReminder r) {
        AppointmentReminderEntity entity = new AppointmentReminderEntity(
                r.id(),
                r.appointmentId(),
                r.scheduledFor(),
                r.sentAt(),
                r.status(),
                r.channel()
        );
        AppointmentReminderEntity saved = reminderRepo.save(entity);
        return new AppointmentReminder(
                saved.getId(),
                saved.getAppointmentId(),
                saved.getScheduledFor(),
                saved.getSentAt(),
                saved.getStatus(),
                saved.getChannel()
        );
    }

    @Override
    public List<AppointmentReminder> findRemindersByAppointmentId(Long appointmentId) {
        return reminderRepo.findByAppointmentId(appointmentId).stream()
                .map(e -> new AppointmentReminder(
                        e.getId(),
                        e.getAppointmentId(),
                        e.getScheduledFor(),
                        e.getSentAt(),
                        e.getStatus(),
                        e.getChannel()
                ))
                .toList();
    }

    @Override
    public List<UpcomingAppointmentView> findUpcomingApprovedAppointments(LocalDateTime from, LocalDateTime to) {
        var statusOpt = statusRepo.findByCode("APPROVED");
        if (statusOpt.isEmpty()) return List.of();
        Short approvedStatusId = statusOpt.get().id;

        List<AppointmentEntity> entities = appointmentRepo.findUpcomingApproved(approvedStatusId, from, to);
        List<UpcomingAppointmentView> views = new ArrayList<>();

        for (AppointmentEntity a : entities) {
            String patientName = a.patient != null ? a.patient.firstName + " " + a.patient.lastName : "Paciente";
            String patientEmail = a.patient != null ? a.patient.email : "";
            String patientPhone = a.patient != null && a.patient.phone != null ? a.patient.phone : "";

            String profName = (a.professional != null && a.professional.user != null)
                    ? "Dr(a). " + a.professional.user.firstName + " " + a.professional.user.lastName
                    : "Profesional Médico";

            String specialtyName = a.specialty != null ? a.specialty.name : "Consulta Médica";
            String locationName = a.location != null ? a.location.name : "Sede FCV";

            views.add(new UpcomingAppointmentView(
                    a.id,
                    a.patientUserId,
                    patientName,
                    patientEmail,
                    patientPhone,
                    a.professionalId,
                    profName,
                    specialtyName,
                    locationName,
                    a.scheduledStartAt,
                    a.scheduledEndAt,
                    "APPROVED"
            ));
        }

        return views;
    }

    @Override
    public List<Appointment> findAppointmentsForDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return appointmentRepo.findForDate(start, end).stream()
                .map(this::toAppointment)
                .toList();
    }

    private NotificationEvent toEvent(NotificationEventEntity e) {
        return new NotificationEvent(
                e.getId(),
                e.getEventType(),
                e.getAggregateId(),
                e.getPayload(),
                e.getStatus(),
                e.getRetryCount(),
                e.getCreatedAt(),
                e.getProcessedAt()
        );
    }

    private Appointment toAppointment(AppointmentEntity e) {
        return new Appointment(
                e.id,
                e.patientUserId,
                e.professionalId,
                e.locationId,
                e.specialtyId,
                e.statusId,
                e.scheduledStartAt,
                e.scheduledEndAt,
                e.rejectionReason,
                e.createdAt,
                e.updatedAt
        );
    }
}
