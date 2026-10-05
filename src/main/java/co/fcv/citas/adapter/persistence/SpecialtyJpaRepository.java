package co.fcv.citas.adapter.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecialtyJpaRepository extends JpaRepository<SpecialtyEntity, Short> {
    Optional<SpecialtyEntity> findByCode(String code);
}
