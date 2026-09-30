package co.fcv.citas.adapter.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentStatusJpaRepository extends JpaRepository<AppointmentStatusEntity, Short> {
    Optional<AppointmentStatusEntity> findByCode(String code);
}
