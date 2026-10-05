package co.fcv.citas.adapter.persistence;

import co.fcv.citas.application.SchedulingPorts;
import co.fcv.citas.domain.Appointment;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AppointmentPersistenceAdapter implements SchedulingPorts.Appointments {
    private final AppointmentJpaRepository repository;

    public AppointmentPersistenceAdapter(AppointmentJpaRepository repository) {
        this.repository = repository;
    }

    public Appointment save(Appointment a) {
        AppointmentEntity e = new AppointmentEntity();
        e.id = a.id();
        e.patientUserId = a.patientUserId();
        e.professionalId = a.professionalId();
        e.locationId = a.locationId();
        e.specialtyId = a.specialtyId();
        e.statusId = a.statusId();
        e.scheduledStartAt = a.scheduledStartAt();
        e.scheduledEndAt = a.scheduledEndAt();
        e.rejectionReason = a.rejectionReason();
        e.createdAt = a.createdAt();
        e.updatedAt = a.updatedAt();

        AppointmentEntity saved = repository.saveAndFlush(e);
        return map(saved);
    }

    public Optional<Appointment> findById(Long id) {
        return repository.findById(id).map(this::map);
    }

    public List<Appointment> findByPatientUserId(Long patientUserId) {
        return repository.findByPatientUserIdOrderByScheduledStartAtDesc(patientUserId)
                .stream().map(this::map).toList();
    }

    public List<Appointment> findByProfessionalId(Long professionalId, LocalDate date, Short statusId) {
        LocalDateTime startOfDay = date != null ? date.atStartOfDay() : null;
        LocalDateTime endOfDay = date != null ? date.atTime(LocalTime.MAX) : null;
        return repository.findForProfessional(professionalId, statusId, startOfDay, endOfDay)
                .stream().map(this::map).toList();
    }

    public List<Appointment> findByFilters(Short statusId, Short locationId, Long professionalId, LocalDate date) {
        LocalDateTime startOfDay = date != null ? date.atStartOfDay() : null;
        LocalDateTime endOfDay = date != null ? date.atTime(LocalTime.MAX) : null;
        return repository.findFiltered(statusId, locationId, professionalId, startOfDay, endOfDay)
                .stream().map(this::map).toList();
    }

    private Appointment map(AppointmentEntity e) {
        return new Appointment(
                e.id, e.patientUserId, e.professionalId, e.locationId, e.specialtyId,
                e.statusId, e.scheduledStartAt, e.scheduledEndAt, e.rejectionReason,
                e.createdAt, e.updatedAt
        );
    }
}
