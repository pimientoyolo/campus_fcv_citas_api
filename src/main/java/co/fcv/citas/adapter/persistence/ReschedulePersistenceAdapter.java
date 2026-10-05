package co.fcv.citas.adapter.persistence;

import co.fcv.citas.application.SchedulingPorts;
import co.fcv.citas.domain.AppointmentReschedule;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ReschedulePersistenceAdapter implements SchedulingPorts.Reschedules {
    private final AppointmentRescheduleJpaRepository repository;

    public ReschedulePersistenceAdapter(AppointmentRescheduleJpaRepository repository) {
        this.repository = repository;
    }

    public AppointmentReschedule save(AppointmentReschedule r) {
        AppointmentRescheduleEntity e = r.id() != null ? repository.findById(r.id()).orElseGet(AppointmentRescheduleEntity::new) : new AppointmentRescheduleEntity();
        e.appointmentId = r.appointmentId();
        e.requestedByUserId = r.requestedByUserId();
        e.oldStartAt = r.oldStartAt();
        e.newStartAt = r.newStartAt();
        e.newEndAt = r.newEndAt();
        e.statusId = r.statusId();
        e.reason = r.reason();
        e.rejectionReason = r.rejectionReason();
        e.updatedAt = Instant.now();
        AppointmentRescheduleEntity saved = repository.saveAndFlush(e);
        return map(saved);
    }

    public Optional<AppointmentReschedule> findById(Long id) {
        return repository.findById(id).map(this::map);
    }

    public List<AppointmentReschedule> findByAppointmentId(Long appointmentId) {
        return repository.findByAppointmentId(appointmentId).stream().map(this::map).toList();
    }

    public List<AppointmentReschedule> findByStatusId(Short statusId) {
        return repository.findByStatusId(statusId).stream().map(this::map).toList();
    }

    private AppointmentReschedule map(AppointmentRescheduleEntity e) {
        String statusCode = switch (e.statusId) {
            case 1 -> "PENDING";
            case 2 -> "APPROVED";
            case 3 -> "REJECTED";
            default -> "UNKNOWN";
        };
        return new AppointmentReschedule(e.id, e.appointmentId, e.requestedByUserId, e.oldStartAt,
                e.newStartAt, e.newEndAt, e.statusId, statusCode, e.reason, e.rejectionReason, e.createdAt, e.updatedAt);
    }
}
