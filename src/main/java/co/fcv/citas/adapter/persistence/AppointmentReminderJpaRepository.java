package co.fcv.citas.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppointmentReminderJpaRepository extends JpaRepository<AppointmentReminderEntity, Long> {
    List<AppointmentReminderEntity> findByAppointmentId(Long appointmentId);
    List<AppointmentReminderEntity> findByStatus(String status);
}
