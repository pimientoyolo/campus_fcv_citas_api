package co.fcv.citas.adapter.persistence;

import co.fcv.citas.application.SchedulingPorts;
import co.fcv.citas.domain.*;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class CatalogPersistenceAdapter implements SchedulingPorts.Catalogs {
    private final LocationJpaRepository locations;
    private final SpecialtyJpaRepository specialties;
    private final AppointmentStatusJpaRepository statuses;

    public CatalogPersistenceAdapter(LocationJpaRepository locations,
                                     SpecialtyJpaRepository specialties,
                                     AppointmentStatusJpaRepository statuses) {
        this.locations = locations;
        this.specialties = specialties;
        this.statuses = statuses;
    }

    public List<Location> findAllLocations() {
        return locations.findAll().stream().map(this::map).toList();
    }

    public Optional<Location> findLocationById(Short id) {
        return locations.findById(id).map(this::map);
    }

    public List<Specialty> findAllSpecialties() {
        return specialties.findAll().stream().map(this::map).toList();
    }

    public Optional<Specialty> findSpecialtyById(Short id) {
        return specialties.findById(id).map(this::map);
    }

    public List<AppointmentStatus> findAllAppointmentStatuses() {
        return statuses.findAll().stream().map(this::map).toList();
    }

    public Optional<AppointmentStatus> findStatusById(Short id) {
        return statuses.findById(id).map(this::map);
    }

    public Optional<AppointmentStatus> findStatusByCode(String code) {
        return statuses.findByCode(code).map(this::map);
    }

    private Location map(LocationEntity e) {
        return new Location(e.id, e.code, e.name, e.address, e.active);
    }

    private Specialty map(SpecialtyEntity e) {
        return new Specialty(e.id, e.code, e.name, e.appointmentDurationMinutes, e.isGeneral, e.requiresAdminApproval, e.active);
    }

    private AppointmentStatus map(AppointmentStatusEntity e) {
        return new AppointmentStatus(e.id, e.code, e.name, e.isTerminal);
    }
}
