package co.fcv.citas.adapter.persistence;

import co.fcv.citas.application.SchedulingPorts;
import co.fcv.citas.domain.*;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ProfessionalPersistenceAdapter implements SchedulingPorts.Professionals {
    private final ProfessionalJpaRepository repository;
    private final SpecialtyJpaRepository specialtyRepository;
    private final LocationJpaRepository locationRepository;
    private final UserJpaRepository userRepository;

    public ProfessionalPersistenceAdapter(ProfessionalJpaRepository repository,
                                         SpecialtyJpaRepository specialtyRepository,
                                         LocationJpaRepository locationRepository,
                                         UserJpaRepository userRepository) {
        this.repository = repository;
        this.specialtyRepository = specialtyRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
    }

    public List<Professional> findAll(Boolean active, Short specialtyId, Short locationId) {
        return repository.findFiltered(active, specialtyId, locationId).stream().map(this::map).toList();
    }

    public Optional<Professional> findById(Long id) {
        return repository.findById(id).map(this::map);
    }

    public Optional<Professional> findByUserId(Long userId) {
        return repository.findByUserId(userId).map(this::map);
    }

    public Professional create(Long userId, String professionalCode, String licenseNumber, List<Short> specialtyIds, List<Short> locationIds) {
        ProfessionalEntity e = new ProfessionalEntity();
        e.userId = userId;
        e.user = userRepository.findById(userId).orElse(null);
        e.professionalCode = professionalCode;
        e.licenseNumber = licenseNumber;
        e.active = true;
        if (specialtyIds != null && !specialtyIds.isEmpty()) {
            e.specialties = specialtyRepository.findAllById(specialtyIds);
        }
        if (locationIds != null && !locationIds.isEmpty()) {
            e.locations = locationRepository.findAllById(locationIds);
        }
        return map(repository.saveAndFlush(e));
    }

    public Professional save(Professional p) {
        ProfessionalEntity e = new ProfessionalEntity();
        e.id = p.id();
        e.userId = p.userId();
        e.professionalCode = p.professionalCode();
        e.licenseNumber = p.licenseNumber();
        e.active = p.active();
        return map(repository.saveAndFlush(e));
    }

    private Professional map(ProfessionalEntity e) {
        List<Specialty> specs = e.specialties.stream()
                .map(s -> new Specialty(s.id, s.code, s.name, s.appointmentDurationMinutes, s.isGeneral, s.requiresAdminApproval, s.active))
                .toList();
        List<Location> locs = e.locations.stream()
                .map(l -> new Location(l.id, l.code, l.name, l.address, l.active))
                .toList();

        String firstName = e.user != null ? e.user.firstName : "";
        String lastName = e.user != null ? e.user.lastName : "";
        String email = e.user != null ? e.user.email : "";

        return new Professional(e.id, e.userId, e.professionalCode, e.licenseNumber, e.active, firstName, lastName, email, specs, locs);
    }
}
