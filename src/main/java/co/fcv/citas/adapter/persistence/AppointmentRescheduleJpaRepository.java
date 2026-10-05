package co.fcv.citas.adapter.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRescheduleJpaRepository extends JpaRepository<AppointmentRescheduleEntity, Long> {
    List<AppointmentRescheduleEntity> findByAppointmentId(Long appointmentId);
    List<AppointmentRescheduleEntity> findByStatusId(Short statusId);
}
