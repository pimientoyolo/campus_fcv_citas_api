package co.fcv.citas.adapter.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpsPlanJpaRepository extends JpaRepository<EpsPlanEntity, Short> {
    List<EpsPlanEntity> findByEpsIdAndActiveTrue(Short epsId);
    List<EpsPlanEntity> findByEpsId(Short epsId);
    Optional<EpsPlanEntity> findByEpsIdAndCode(Short epsId, String code);
}
