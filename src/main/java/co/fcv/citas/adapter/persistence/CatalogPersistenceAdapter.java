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
    private final RegimenJpaRepository regimens;
    private final EpsJpaRepository epsList;
    private final EpsPlanJpaRepository epsPlans;

    public CatalogPersistenceAdapter(LocationJpaRepository locations,
                                     SpecialtyJpaRepository specialties,
                                     AppointmentStatusJpaRepository statuses,
                                     RegimenJpaRepository regimens,
                                     EpsJpaRepository epsList,
                                     EpsPlanJpaRepository epsPlans) {
        this.locations = locations;
        this.specialties = specialties;
        this.statuses = statuses;
        this.regimens = regimens;
        this.epsList = epsList;
        this.epsPlans = epsPlans;
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

    public Specialty saveSpecialty(Specialty s) {
        SpecialtyEntity e = s.id() != null ? specialties.findById(s.id()).orElseGet(SpecialtyEntity::new) : new SpecialtyEntity();
        e.code = s.code();
        e.name = s.name();
        e.appointmentDurationMinutes = (short) s.durationMinutes();
        e.isGeneral = s.isGeneral();
        e.requiresAdminApproval = s.requiresAdminApproval();
        e.active = s.active();
        return map(specialties.saveAndFlush(e));
    }

    public List<Regimen> findAllRegimens() {
        return regimens.findAll().stream().map(r -> new Regimen(r.id, r.code, r.name, r.active)).toList();
    }

    public List<Eps> findAllEps() {
        return epsList.findAll().stream().map(e -> new Eps(e.id, e.code, e.name, e.active)).toList();
    }

    public Optional<Eps> findEpsById(Short id) {
        return epsList.findById(id).map(e -> new Eps(e.id, e.code, e.name, e.active));
    }

    public Eps saveEps(Eps eps) {
        EpsEntity e = eps.id() != null ? epsList.findById(eps.id()).orElseGet(EpsEntity::new) : new EpsEntity();
        e.code = eps.code();
        e.name = eps.name();
        e.active = eps.active();
        EpsEntity saved = epsList.saveAndFlush(e);
        return new Eps(saved.id, saved.code, saved.name, saved.active);
    }

    public List<EpsPlan> findPlansByEpsId(Short epsId) {
        return epsPlans.findByEpsId(epsId).stream()
                .map(p -> new EpsPlan(p.id, p.epsId, p.code, p.name, p.active)).toList();
    }

    public Optional<EpsPlan> findPlanById(Short id) {
        return epsPlans.findById(id).map(p -> new EpsPlan(p.id, p.epsId, p.code, p.name, p.active));
    }

    public EpsPlan savePlan(EpsPlan plan) {
        EpsPlanEntity e = plan.id() != null ? epsPlans.findById(plan.id()).orElseGet(EpsPlanEntity::new) : new EpsPlanEntity();
        e.epsId = plan.epsId();
        e.code = plan.code();
        e.name = plan.name();
        e.active = plan.active();
        EpsPlanEntity saved = epsPlans.saveAndFlush(e);
        return new EpsPlan(saved.id, saved.epsId, saved.code, saved.name, saved.active);
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
