package co.fcv.citas.adapter.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfessionalJpaRepository extends JpaRepository<ProfessionalEntity, Long> {
    Optional<ProfessionalEntity> findByUserId(Long userId);
    Optional<ProfessionalEntity> findByProfessionalCode(String code);

    @Query("""
        SELECT DISTINCT p FROM ProfessionalEntity p
        LEFT JOIN p.specialties s
        LEFT JOIN p.locations l
        WHERE (:active IS NULL OR p.active = :active)
          AND (:specialtyId IS NULL OR s.id = :specialtyId)
          AND (:locationId IS NULL OR l.id = :locationId)
    """)
    List<ProfessionalEntity> findFiltered(
        @Param("active") Boolean active,
        @Param("specialtyId") Short specialtyId,
        @Param("locationId") Short locationId
    );
}
