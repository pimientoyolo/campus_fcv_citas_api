package co.fcv.citas.adapter.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegimenJpaRepository extends JpaRepository<RegimenEntity, Short> {
    List<RegimenEntity> findByActiveTrue();
    Optional<RegimenEntity> findByCode(String code);
}
