package co.fcv.citas.adapter.persistence;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilityBlockJpaRepository extends JpaRepository<AvailabilityBlockEntity, Long> {
    List<AvailabilityBlockEntity> findByProfessionalId(Long professionalId);
    List<AvailabilityBlockEntity> findByProfessionalIdAndAvailableDate(Long professionalId, LocalDate availableDate);
}
