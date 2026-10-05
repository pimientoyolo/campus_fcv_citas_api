package co.fcv.citas.adapter.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpsJpaRepository extends JpaRepository<EpsEntity, Short> {
    List<EpsEntity> findByActiveTrue();
    Optional<EpsEntity> findByCode(String code);
}
