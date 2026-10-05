package co.fcv.citas.adapter.persistence;

import co.fcv.citas.application.SchedulingPorts;
import co.fcv.citas.domain.AppointmentStatusHistory;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AppointmentHistoryPersistenceAdapter implements SchedulingPorts.AppointmentHistories {
    private final AppointmentStatusHistoryJpaRepository repository;

    public AppointmentHistoryPersistenceAdapter(AppointmentStatusHistoryJpaRepository repository) {
        this.repository = repository;
    }

    public void record(AppointmentStatusHistory h) {
        AppointmentStatusHistoryEntity e = new AppointmentStatusHistoryEntity();
        e.id = h.id();
        e.appointmentId = h.appointmentId();
        e.statusId = h.statusId();
        e.changedByUserId = h.changedByUserId();
        e.changeSource = h.changeSource();
        e.changedAt = h.changedAt();
        e.reason = h.reason();
        repository.saveAndFlush(e);
    }

    public List<AppointmentStatusHistory> findByAppointmentId(Long appointmentId) {
        return repository.findByAppointmentIdOrderByChangedAtAsc(appointmentId)
                .stream().map(e -> new AppointmentStatusHistory(
                        e.id, e.appointmentId, e.statusId, e.changedByUserId, e.changeSource, e.changedAt, e.reason
                )).toList();
    }
}
