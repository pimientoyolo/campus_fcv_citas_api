package co.fcv.citas.adapter.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationJpaRepository extends JpaRepository<LocationEntity, Short> {
    Optional<LocationEntity> findByCode(String code);
}
