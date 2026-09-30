package co.fcv.citas.adapter.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentStatusHistoryJpaRepository extends JpaRepository<AppointmentStatusHistoryEntity, Long> {
    List<AppointmentStatusHistoryEntity> findByAppointmentIdOrderByChangedAtAsc(Long appointmentId);
}
