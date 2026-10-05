package co.fcv.citas.adapter.persistence;

import co.fcv.citas.application.SchedulingPorts;
import co.fcv.citas.domain.ProfessionalSlot;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ProfessionalSlotPersistenceAdapter implements SchedulingPorts.ProfessionalSlots {
    private final ProfessionalSlotJpaRepository repository;

    public ProfessionalSlotPersistenceAdapter(ProfessionalSlotJpaRepository repository) {
        this.repository = repository;
    }

    public List<ProfessionalSlot> findByBlockId(Long blockId) {
        return repository.findByAvailabilityBlockId(blockId).stream().map(this::map).toList();
    }

    public List<ProfessionalSlot> findAvailableSlots(Short locationId, Long professionalId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return repository.findAvailable(locationId, professionalId, startOfDay, endOfDay).stream().map(this::map).toList();
    }

    public List<ProfessionalSlot> findSlotsForRange(Long professionalId, LocalDateTime startAt, LocalDateTime endAt) {
        return repository.findForRange(professionalId, startAt, endAt).stream().map(this::map).toList();
    }

    public List<ProfessionalSlot> findSlotsByAppointmentId(Long appointmentId) {
        return repository.findByAppointmentId(appointmentId).stream().map(this::map).toList();
    }

    public void saveAll(List<ProfessionalSlot> slots) {
        List<ProfessionalSlotEntity> entities = slots.stream().map(s -> {
            ProfessionalSlotEntity e = new ProfessionalSlotEntity();
            e.id = s.id();
            e.availabilityBlockId = s.availabilityBlockId();
            e.appointmentId = s.appointmentId();
            e.startAt = s.startAt();
            e.endAt = s.endAt();
            return e;
        }).toList();
        repository.saveAllAndFlush(entities);
    }

    public int assignSlots(List<Long> slotIds, Long appointmentId) {
        return repository.assignSlots(slotIds, appointmentId);
    }

    public void releaseSlots(Long appointmentId) {
        repository.releaseSlots(appointmentId);
    }

    private ProfessionalSlot map(ProfessionalSlotEntity e) {
        Long profId = e.availabilityBlock != null ? e.availabilityBlock.professionalId : null;
        return new ProfessionalSlot(e.id, e.availabilityBlockId, profId, e.appointmentId, e.startAt, e.endAt);
    }
}
