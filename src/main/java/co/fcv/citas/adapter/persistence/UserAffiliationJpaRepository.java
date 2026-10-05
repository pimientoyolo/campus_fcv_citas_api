package co.fcv.citas.adapter.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAffiliationJpaRepository extends JpaRepository<UserAffiliationEntity, Long> {
    List<UserAffiliationEntity> findByUserIdAndActiveTrue(Long userId);
    List<UserAffiliationEntity> findByUserId(Long userId);
    Optional<UserAffiliationEntity> findByUserIdAndEpsIdAndEpsPlanIdAndRegimenId(Long userId, Short epsId, Short epsPlanId, Short regimenId);
}
