package co.fcv.citas.adapter.persistence;

import co.fcv.citas.application.SchedulingPorts;
import co.fcv.citas.domain.UserAffiliation;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AffiliationPersistenceAdapter implements SchedulingPorts.Affiliations {
    private final UserAffiliationJpaRepository repository;

    public AffiliationPersistenceAdapter(UserAffiliationJpaRepository repository) {
        this.repository = repository;
    }

    public List<UserAffiliation> findByUserId(Long userId) {
        return repository.findByUserIdAndActiveTrue(userId).stream().map(this::map).toList();
    }

    public Optional<UserAffiliation> findExisting(Long userId, Short epsId, Short epsPlanId, Short regimenId) {
        return repository.findByUserIdAndEpsIdAndEpsPlanIdAndRegimenId(userId, epsId, epsPlanId, regimenId).map(this::map);
    }

    public UserAffiliation save(UserAffiliation a) {
        UserAffiliationEntity e = a.id() != null ? repository.findById(a.id()).orElseGet(UserAffiliationEntity::new) : new UserAffiliationEntity();
        e.userId = a.userId();
        e.epsId = a.epsId();
        e.epsPlanId = a.epsPlanId();
        e.regimenId = a.regimenId();
        e.active = a.active();
        UserAffiliationEntity saved = repository.saveAndFlush(e);
        return repository.findById(saved.id).map(this::map).orElseGet(() -> map(saved));
    }

    public void deactivate(Long id) {
        repository.findById(id).ifPresent(e -> {
            e.active = false;
            repository.saveAndFlush(e);
        });
    }

    private UserAffiliation map(UserAffiliationEntity e) {
        String epsName = e.eps != null ? e.eps.name : "EPS " + e.epsId;
        String planName = e.epsPlan != null ? e.epsPlan.name : "Plan " + e.epsPlanId;
        String regimenName = e.regimen != null ? e.regimen.name : "Régimen " + e.regimenId;
        return new UserAffiliation(e.id, e.userId, e.epsId, epsName, e.epsPlanId, planName, e.regimenId, regimenName, e.active, e.createdAt);
    }
}
