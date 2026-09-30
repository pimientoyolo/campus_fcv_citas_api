package co.fcv.citas.adapter.persistence;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentJpaRepository extends JpaRepository<AppointmentEntity, Long> {
    List<AppointmentEntity> findByPatientUserIdOrderByScheduledStartAtDesc(Long patientUserId);

    @Query("""
        SELECT a FROM AppointmentEntity a
        WHERE a.professionalId = :professionalId
          AND (:statusId IS NULL OR a.statusId = :statusId)
          AND (:startOfDay IS NULL OR (a.scheduledStartAt >= :startOfDay AND a.scheduledStartAt < :endOfDay))
        ORDER BY a.scheduledStartAt ASC
    """)
    List<AppointmentEntity> findForProfessional(
        @Param("professionalId") Long professionalId,
        @Param("statusId") Short statusId,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("""
        SELECT a FROM AppointmentEntity a
        WHERE (:statusId IS NULL OR a.statusId = :statusId)
          AND (:locationId IS NULL OR a.locationId = :locationId)
          AND (:professionalId IS NULL OR a.professionalId = :professionalId)
          AND (:startOfDay IS NULL OR (a.scheduledStartAt >= :startOfDay AND a.scheduledStartAt < :endOfDay))
        ORDER BY a.scheduledStartAt ASC
    """)
    List<AppointmentEntity> findFiltered(
        @Param("statusId") Short statusId,
        @Param("locationId") Short locationId,
        @Param("professionalId") Long professionalId,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );
}
