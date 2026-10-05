package co.fcv.citas.adapter.persistence;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfessionalSlotJpaRepository extends JpaRepository<ProfessionalSlotEntity, Long> {
    List<ProfessionalSlotEntity> findByAvailabilityBlockId(Long availabilityBlockId);

    @Query("""
        SELECT s FROM ProfessionalSlotEntity s
        JOIN AvailabilityBlockEntity b ON s.availabilityBlockId = b.id
        WHERE s.appointmentId IS NULL
          AND b.active = true
          AND (:locationId IS NULL OR b.locationId = :locationId)
          AND (:professionalId IS NULL OR b.professionalId = :professionalId)
          AND s.startAt >= :startOfDay AND s.startAt < :endOfDay
        ORDER BY s.startAt ASC
    """)
    List<ProfessionalSlotEntity> findAvailable(
        @Param("locationId") Short locationId,
        @Param("professionalId") Long professionalId,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("""
        SELECT s FROM ProfessionalSlotEntity s
        JOIN AvailabilityBlockEntity b ON s.availabilityBlockId = b.id
        WHERE b.professionalId = :professionalId
          AND s.startAt >= :startAt AND s.endAt <= :endAt
        ORDER BY s.startAt ASC
    """)
    List<ProfessionalSlotEntity> findForRange(
        @Param("professionalId") Long professionalId,
        @Param("startAt") LocalDateTime startAt,
        @Param("endAt") LocalDateTime endAt
    );

    List<ProfessionalSlotEntity> findByAppointmentId(Long appointmentId);

    @Modifying
    @Query("UPDATE ProfessionalSlotEntity s SET s.appointmentId = :appointmentId WHERE s.id IN :slotIds AND s.appointmentId IS NULL")
    int assignSlots(@Param("slotIds") List<Long> slotIds, @Param("appointmentId") Long appointmentId);

    @Modifying
    @Query("UPDATE ProfessionalSlotEntity s SET s.appointmentId = NULL WHERE s.appointmentId = :appointmentId")
    void releaseSlots(@Param("appointmentId") Long appointmentId);
}
