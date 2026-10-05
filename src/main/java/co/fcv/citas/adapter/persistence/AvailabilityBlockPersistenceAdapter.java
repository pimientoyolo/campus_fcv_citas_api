package co.fcv.citas.adapter.persistence;

import co.fcv.citas.application.SchedulingPorts;
import co.fcv.citas.domain.AvailabilityBlock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AvailabilityBlockPersistenceAdapter implements SchedulingPorts.AvailabilityBlocks {
    private final AvailabilityBlockJpaRepository repository;

    public AvailabilityBlockPersistenceAdapter(AvailabilityBlockJpaRepository repository) {
        this.repository = repository;
    }

    public List<AvailabilityBlock> findByProfessional(Long professionalId) {
        return repository.findByProfessionalId(professionalId).stream().map(this::map).toList();
    }

    public List<AvailabilityBlock> findByProfessionalAndDate(Long professionalId, LocalDate date) {
        return repository.findByProfessionalIdAndAvailableDate(professionalId, date).stream().map(this::map).toList();
    }

    public Optional<AvailabilityBlock> findById(Long id) {
        return repository.findById(id).map(this::map);
    }

    public AvailabilityBlock save(AvailabilityBlock b) {
        AvailabilityBlockEntity e = new AvailabilityBlockEntity();
        e.id = b.id();
        e.professionalId = b.professionalId();
        e.locationId = b.locationId();
        e.availableDate = b.availableDate();
        e.startTime = b.startTime();
        e.endTime = b.endTime();
        e.active = b.active();
        return map(repository.saveAndFlush(e));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private AvailabilityBlock map(AvailabilityBlockEntity e) {
        return new AvailabilityBlock(e.id, e.professionalId, e.locationId, e.availableDate, e.startTime, e.endTime, e.active);
    }
}
