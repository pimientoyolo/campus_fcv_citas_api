package co.fcv.citas.application;

import co.fcv.citas.domain.*;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static co.fcv.citas.application.SchedulingPorts.*;

public class SchedulingService {
    private final Catalogs catalogs;
    private final Professionals professionals;
    private final AvailabilityBlocks availabilityBlocks;
    private final ProfessionalSlots professionalSlots;
    private final Appointments appointments;
    private final AppointmentHistories histories;
    private final AuthPorts.Users users;
    private final AuthPorts.Passwords passwords;
    private final Affiliations affiliations;
    private final Reschedules reschedules;
    private final Integrations integrations;
    private final Clock clock;

    public SchedulingService(Catalogs catalogs, Professionals professionals,
                             AvailabilityBlocks availabilityBlocks,
                             ProfessionalSlots professionalSlots,
                             Appointments appointments,
                             AppointmentHistories histories,
                             AuthPorts.Users users,
                             AuthPorts.Passwords passwords,
                             Clock clock) {
        this(catalogs, professionals, availabilityBlocks, professionalSlots, appointments, histories, users, passwords, null, null, null, clock);
    }

    public SchedulingService(Catalogs catalogs, Professionals professionals,
                             AvailabilityBlocks availabilityBlocks,
                             ProfessionalSlots professionalSlots,
                             Appointments appointments,
                             AppointmentHistories histories,
                             AuthPorts.Users users,
                             AuthPorts.Passwords passwords,
                             Affiliations affiliations,
                             Reschedules reschedules,
                             Clock clock) {
        this(catalogs, professionals, availabilityBlocks, professionalSlots, appointments, histories, users, passwords, affiliations, reschedules, null, clock);
    }

    public SchedulingService(Catalogs catalogs, Professionals professionals,
                             AvailabilityBlocks availabilityBlocks,
                             ProfessionalSlots professionalSlots,
                             Appointments appointments,
                             AppointmentHistories histories,
                             AuthPorts.Users users,
                             AuthPorts.Passwords passwords,
                             Affiliations affiliations,
                             Reschedules reschedules,
                             Integrations integrations,
                             Clock clock) {
        this.catalogs = catalogs;
        this.professionals = professionals;
        this.availabilityBlocks = availabilityBlocks;
        this.professionalSlots = professionalSlots;
        this.appointments = appointments;
        this.histories = histories;
        this.users = users;
        this.passwords = passwords;
        this.affiliations = affiliations;
        this.reschedules = reschedules;
        this.integrations = integrations;
        this.clock = clock;
    }

    // --- Catálogos ---
    public List<Location> getLocations() { return catalogs.findAllLocations(); }
    public List<Specialty> getSpecialties() { return catalogs.findAllSpecialties(); }
    public List<AppointmentStatus> getAppointmentStatuses() { return catalogs.findAllAppointmentStatuses(); }
    public List<Regimen> getRegimens() { return catalogs.findAllRegimens(); }
    public List<Eps> getEpsList() { return catalogs.findAllEps(); }
    public List<EpsPlan> getEpsPlans(Short epsId) { return catalogs.findPlansByEpsId(epsId); }
    public Eps saveEps(Eps eps) { return catalogs.saveEps(eps); }
    public EpsPlan saveEpsPlan(EpsPlan plan) { return catalogs.savePlan(plan); }
    public Specialty saveSpecialty(Specialty specialty) { return catalogs.saveSpecialty(specialty); }

    // --- Profesionales ---
    public record CreateProfessionalCommand(
            String firstName, String lastName, String documentType, String documentNumber,
            String email, String phone, String password,
            String professionalCode, String licenseNumber,
            List<Short> specialtyIds, List<Short> locationIds
    ) {}

    public Professional createProfessional(CreateProfessionalCommand cmd) {
        String hash = passwords.hash(cmd.password());
        User user = users.create(new User(
                null, cmd.firstName().strip(), cmd.lastName().strip(),
                cmd.documentType(), cmd.documentNumber().strip(),
                cmd.email().strip().toLowerCase(Locale.ROOT),
                cmd.phone().strip(), hash, true, Set.of("USER", "PROFESSIONAL")
        ));
        return professionals.create(user.id(), cmd.professionalCode().strip(), cmd.licenseNumber().strip(), cmd.specialtyIds(), cmd.locationIds());
    }

    public List<Professional> listProfessionals(Boolean active, Short specialtyId, Short locationId) {
        return professionals.findAll(active, specialtyId, locationId);
    }

    public Professional getProfessional(Long id) {
        return professionals.findById(id)
                .orElseThrow(() -> SchedulingFailure.notFound("Profesional no encontrado."));
    }

    public Professional getProfessionalByUserId(Long userId) {
        return professionals.findByUserId(userId)
                .orElseThrow(() -> SchedulingFailure.notFound("Perfil profesional no encontrado para este usuario."));
    }

    public record UpdateProfessionalCommand(
            String professionalCode,
            String licenseNumber,
            Boolean active,
            List<Short> specialtyIds,
            List<Short> locationIds
    ) {}

    public Professional updateProfessional(Long id, UpdateProfessionalCommand cmd) {
        Professional existing = getProfessional(id);
        List<Specialty> specs = existing.specialties();
        if (cmd.specialtyIds() != null) {
            specs = cmd.specialtyIds().stream()
                    .map(sid -> catalogs.findSpecialtyById(sid).orElseThrow(() -> SchedulingFailure.notFound("Especialidad no encontrada: " + sid)))
                    .toList();
        }
        List<Location> locs = existing.locations();
        if (cmd.locationIds() != null) {
            locs = cmd.locationIds().stream()
                    .map(lid -> catalogs.findLocationById(lid).orElseThrow(() -> SchedulingFailure.notFound("Sede no encontrada: " + lid)))
                    .toList();
        }
        Professional updated = new Professional(
                existing.id(),
                existing.userId(),
                cmd.professionalCode() != null ? cmd.professionalCode().strip() : existing.professionalCode(),
                cmd.licenseNumber() != null ? cmd.licenseNumber().strip() : existing.licenseNumber(),
                cmd.active() != null ? cmd.active() : existing.active(),
                existing.firstName(),
                existing.lastName(),
                existing.email(),
                specs,
                locs
        );
        return professionals.save(updated);
    }

    // --- Bloques de Disponibilidad ---
    public record CreateBlockCommand(Long professionalId, Short locationId,
                                    LocalDate date, LocalTime startTime, LocalTime endTime) {}

    public AvailabilityBlock createBlock(CreateBlockCommand cmd) {
        LocalDate today = LocalDate.now(clock);
        if (cmd.date().isBefore(today)) {
            throw SchedulingFailure.invalid("No se pueden crear bloques de disponibilidad en el pasado.");
        }
        if (!cmd.startTime().isBefore(cmd.endTime())) {
            throw SchedulingFailure.invalid("La hora de inicio debe ser anterior a la hora de fin.");
        }

        Professional prof = getProfessional(cmd.professionalId());
        boolean hasLocation = prof.locations().stream().anyMatch(l -> l.id().equals(cmd.locationId()));
        if (!hasLocation) {
            throw SchedulingFailure.invalid("El profesional no tiene asignada la sede especificada.");
        }

        List<AvailabilityBlock> existing = availabilityBlocks.findByProfessionalAndDate(cmd.professionalId(), cmd.date());
        for (AvailabilityBlock b : existing) {
            if (b.active() && !(cmd.endTime().compareTo(b.startTime()) <= 0 || cmd.startTime().compareTo(b.endTime()) >= 0)) {
                throw SchedulingFailure.conflict("Ya existe un bloque en ese horario para este profesional.");
            }
        }

        AvailabilityBlock savedBlock = availabilityBlocks.save(
                new AvailabilityBlock(null, cmd.professionalId(), cmd.locationId(), cmd.date(), cmd.startTime(), cmd.endTime(), true));

        // Discretizar en slots de 30 minutos
        List<ProfessionalSlot> slots = new ArrayList<>();
        LocalTime current = cmd.startTime();
        while (current.plusMinutes(30).compareTo(cmd.endTime()) <= 0) {
            LocalDateTime startAt = LocalDateTime.of(cmd.date(), current);
            LocalDateTime endAt = LocalDateTime.of(cmd.date(), current.plusMinutes(30));
            slots.add(new ProfessionalSlot(null, savedBlock.id(), cmd.professionalId(), null, startAt, endAt));
            current = current.plusMinutes(30);
        }
        professionalSlots.saveAll(slots);
        return savedBlock;
    }

    public void deleteBlock(Long blockId, Long requestingUserId, boolean isAdmin) {
        AvailabilityBlock block = availabilityBlocks.findById(blockId)
                .orElseThrow(() -> SchedulingFailure.notFound("Bloque no encontrado."));
        if (!isAdmin) {
            Professional prof = getProfessionalByUserId(requestingUserId);
            if (!prof.id().equals(block.professionalId())) {
                throw SchedulingFailure.forbidden("No tienes permiso para eliminar este bloque.");
            }
        }
        List<ProfessionalSlot> slots = professionalSlots.findByBlockId(blockId);
        boolean hasAppointments = slots.stream().anyMatch(s -> s.appointmentId() != null);
        if (hasAppointments) {
            throw SchedulingFailure.conflict("No se puede eliminar un bloque con citas programadas.");
        }
        availabilityBlocks.delete(blockId);
    }

    public List<AvailabilityBlock> listBlocks(Long professionalId) {
        return availabilityBlocks.findByProfessional(professionalId);
    }

    // --- Consulta de Horarios Disponibles ---
    public record AvailableSlotDto(LocalDateTime startAt, LocalDateTime endAt,
                                  Long professionalId, Short locationId, Short specialtyId) {}

    public List<AvailableSlotDto> searchAvailableSlots(Short locationId, Short specialtyId,
                                                      Long professionalId, LocalDate date) {
        Specialty specialty = catalogs.findSpecialtyById(specialtyId)
                .orElseThrow(() -> SchedulingFailure.notFound("Especialidad no válida."));

        List<ProfessionalSlot> freeSlots = professionalSlots.findAvailableSlots(locationId, professionalId, date);
        LocalDateTime now = LocalDateTime.now(clock);

        int durationMinutes = specialty.durationMinutes();
        List<AvailableSlotDto> results = new ArrayList<>();

        if (durationMinutes == 30) {
            for (ProfessionalSlot slot : freeSlots) {
                if (slot.startAt().isAfter(now)) {
                    Long profId = slot.professionalId() != null ? slot.professionalId() : professionalId;
                    results.add(new AvailableSlotDto(slot.startAt(), slot.endAt(), profId, locationId, specialtyId));
                }
            }
        } else if (durationMinutes == 60) {
            // Requiere dos slots consecutivos en el mismo bloque
            Map<Long, List<ProfessionalSlot>> byBlock = new HashMap<>();
            for (ProfessionalSlot slot : freeSlots) {
                byBlock.computeIfAbsent(slot.availabilityBlockId(), k -> new ArrayList<>()).add(slot);
            }
            for (List<ProfessionalSlot> blockSlots : byBlock.values()) {
                blockSlots.sort(Comparator.comparing(ProfessionalSlot::startAt));
                for (int i = 0; i < blockSlots.size() - 1; i++) {
                    ProfessionalSlot a = blockSlots.get(i);
                    ProfessionalSlot b = blockSlots.get(i + 1);
                    if (a.endAt().equals(b.startAt()) && a.startAt().isAfter(now)) {
                        Long profId = a.professionalId() != null ? a.professionalId() : professionalId;
                        results.add(new AvailableSlotDto(a.startAt(), b.endAt(), profId, locationId, specialtyId));
                    }
                }
            }
        }
        return results;
    }

    // --- Citas (Appointments) ---
    public record BookAppointmentCommand(Long patientUserId, Long professionalId,
                                         Short locationId, Short specialtyId,
                                         LocalDateTime startAt) {}

    public Appointment bookAppointment(BookAppointmentCommand cmd) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (cmd.startAt().isBefore(now)) {
            throw SchedulingFailure.invalid("No se puede agendar una cita en el pasado.");
        }

        Specialty specialty = catalogs.findSpecialtyById(cmd.specialtyId())
                .orElseThrow(() -> SchedulingFailure.notFound("Especialidad no encontrada."));
        if (!specialty.active()) {
            throw SchedulingFailure.invalid("La especialidad seleccionada no está activa.");
        }

        Professional prof = getProfessional(cmd.professionalId());
        if (!prof.active()) {
            throw SchedulingFailure.invalid("El profesional no se encuentra activo.");
        }
        boolean hasSpecialty = prof.specialties().stream().anyMatch(s -> s.id().equals(cmd.specialtyId()));
        if (!hasSpecialty) {
            throw SchedulingFailure.invalid("El profesional no tiene asignada la especialidad seleccionada.");
        }
        boolean hasLocation = prof.locations().stream().anyMatch(l -> l.id().equals(cmd.locationId()));
        if (!hasLocation) {
            throw SchedulingFailure.invalid("El profesional no atiende en la sede seleccionada.");
        }

        LocalDateTime endAt = cmd.startAt().plusMinutes(specialty.durationMinutes());
        List<ProfessionalSlot> requiredSlots = professionalSlots.findSlotsForRange(cmd.professionalId(), cmd.startAt(), endAt);

        int expectedSlotsCount = specialty.durationMinutes() / 30;
        if (requiredSlots.size() != expectedSlotsCount) {
            throw SchedulingFailure.conflict("El horario seleccionado no cuenta con slots disponibles continuos.");
        }
        for (ProfessionalSlot s : requiredSlots) {
            if (!s.isAvailable()) {
                throw SchedulingFailure.conflict("Uno o más slots del horario seleccionado ya se encuentran ocupados.");
            }
        }

        // RN-02 y RN-03: Cita general se aprueba automáticamente; especializada queda en REQUESTED
        String statusCode = (specialty.isGeneral() || !specialty.requiresAdminApproval())
                ? AppointmentStatus.APPROVED
                : AppointmentStatus.REQUESTED;

        AppointmentStatus status = catalogs.findStatusByCode(statusCode)
                .orElseThrow(() -> SchedulingFailure.invalid("Estado no configurado: " + statusCode));

        Appointment appointment = appointments.save(new Appointment(
                null, cmd.patientUserId(), cmd.professionalId(), cmd.locationId(), cmd.specialtyId(),
                status.id(), cmd.startAt(), endAt, null, null, null));

        // Retener/asignar los slots a la cita creada de forma atómica y verificar concurrencia
        List<Long> slotIds = requiredSlots.stream().map(ProfessionalSlot::id).toList();
        int assigned = professionalSlots.assignSlots(slotIds, appointment.id());
        if (assigned != slotIds.size()) {
            throw SchedulingFailure.conflict("Uno o más turnos seleccionados ya fueron tomados simultáneamente por otra reserva.");
        }

        // Registrar auditoría de estado inicial
        histories.record(new AppointmentStatusHistory(
                null, appointment.id(), status.id(), cmd.patientUserId(), "USER", null,
                statusCode.equals(AppointmentStatus.APPROVED) ? "Cita general auto-aprobada" : "Solicitud de cita especializada"));

        return appointment;
    }

    public Appointment cancelAppointment(Long appointmentId, Long userId, boolean isAdmin) {
        Appointment app = appointments.findById(appointmentId)
                .orElseThrow(() -> SchedulingFailure.notFound("Cita no encontrada."));

        if (!isAdmin && !app.patientUserId().equals(userId)) {
            throw SchedulingFailure.forbidden("No tienes permiso para cancelar esta cita.");
        }

        AppointmentStatus currentStatus = catalogs.findStatusById(app.statusId())
                .orElseThrow(() -> SchedulingFailure.invalid("Estado actual inválido."));
        if (currentStatus.isTerminal()) {
            throw SchedulingFailure.invalid("No se puede cancelar una cita que ya finalizó o fue cancelada.");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        if (app.scheduledStartAt().isBefore(now)) {
            throw SchedulingFailure.invalid("No se puede cancelar una cita en el pasado.");
        }

        AppointmentStatus cancelled = catalogs.findStatusByCode(AppointmentStatus.CANCELLED)
                .orElseThrow(() -> SchedulingFailure.invalid("Estado CANCELLED no configurado."));

        Appointment updated = appointments.save(new Appointment(
                app.id(), app.patientUserId(), app.professionalId(), app.locationId(), app.specialtyId(),
                cancelled.id(), app.scheduledStartAt(), app.scheduledEndAt(), app.rejectionReason(),
                app.createdAt(), null));

        // Liberar slots reservados
        professionalSlots.releaseSlots(appointmentId);

        histories.record(new AppointmentStatusHistory(
                null, app.id(), cancelled.id(), userId, isAdmin ? "ADMIN" : "USER", null,
                isAdmin ? "Cancelada por administración" : "Cancelada por el paciente"));

        publishNotification("APPOINTMENT_CANCELLED", updated.id(),
                "{\"appointmentId\":" + updated.id() + ",\"cancelledBy\":" + userId + "}");

        return updated;
    }

    public Appointment approveAppointment(Long appointmentId, Long adminUserId) {
        Appointment app = appointments.findById(appointmentId)
                .orElseThrow(() -> SchedulingFailure.notFound("Cita no encontrada."));

        AppointmentStatus currentStatus = catalogs.findStatusById(app.statusId())
                .orElseThrow(() -> SchedulingFailure.invalid("Estado actual inválido."));
        if (!AppointmentStatus.REQUESTED.equals(currentStatus.code())) {
            throw SchedulingFailure.invalid("Solo se pueden aprobar citas en estado REQUESTED.");
        }

        AppointmentStatus approved = catalogs.findStatusByCode(AppointmentStatus.APPROVED)
                .orElseThrow(() -> SchedulingFailure.invalid("Estado APPROVED no configurado."));

        Appointment updated = appointments.save(new Appointment(
                app.id(), app.patientUserId(), app.professionalId(), app.locationId(), app.specialtyId(),
                approved.id(), app.scheduledStartAt(), app.scheduledEndAt(), null,
                app.createdAt(), null));

        histories.record(new AppointmentStatusHistory(
                null, app.id(), approved.id(), adminUserId, "ADMIN", null, "Aprobada por administración"));

        publishNotification("APPOINTMENT_APPROVED", updated.id(),
                "{\"appointmentId\":" + updated.id() + ",\"patientUserId\":" + updated.patientUserId() + ",\"approvedBy\":" + adminUserId + "}");

        return updated;
    }

    public Appointment rejectAppointment(Long appointmentId, Long adminUserId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw SchedulingFailure.invalid("El motivo de rechazo es obligatorio (RN-04).");
        }

        Appointment app = appointments.findById(appointmentId)
                .orElseThrow(() -> SchedulingFailure.notFound("Cita no encontrada."));

        AppointmentStatus currentStatus = catalogs.findStatusById(app.statusId())
                .orElseThrow(() -> SchedulingFailure.invalid("Estado actual inválido."));
        if (!AppointmentStatus.REQUESTED.equals(currentStatus.code())) {
            throw SchedulingFailure.invalid("Solo se pueden rechazar citas en estado REQUESTED.");
        }

        AppointmentStatus rejected = catalogs.findStatusByCode(AppointmentStatus.REJECTED)
                .orElseThrow(() -> SchedulingFailure.invalid("Estado REJECTED no configurado."));

        Appointment updated = appointments.save(new Appointment(
                app.id(), app.patientUserId(), app.professionalId(), app.locationId(), app.specialtyId(),
                rejected.id(), app.scheduledStartAt(), app.scheduledEndAt(), reason.strip(),
                app.createdAt(), null));

        // Liberar slots
        professionalSlots.releaseSlots(appointmentId);

        histories.record(new AppointmentStatusHistory(
                null, app.id(), rejected.id(), adminUserId, "ADMIN", null, reason.strip()));

        publishNotification("APPOINTMENT_REJECTED", updated.id(),
                "{\"appointmentId\":" + updated.id() + ",\"patientUserId\":" + updated.patientUserId() + ",\"reason\":\"" + reason.strip() + "\"}");

        return updated;
    }

    public Appointment completeAppointment(Long appointmentId, Long professionalUserId) {
        Appointment app = appointments.findById(appointmentId)
                .orElseThrow(() -> SchedulingFailure.notFound("Cita no encontrada."));

        Professional prof = getProfessionalByUserId(professionalUserId);
        if (!app.professionalId().equals(prof.id())) {
            throw SchedulingFailure.forbidden("Solo el profesional asignado puede marcar la cita como completada.");
        }

        AppointmentStatus currentStatus = catalogs.findStatusById(app.statusId())
                .orElseThrow(() -> SchedulingFailure.invalid("Estado actual inválido."));
        if (!AppointmentStatus.APPROVED.equals(currentStatus.code())) {
            throw SchedulingFailure.invalid("Solo se pueden completar citas previamente aprobadas.");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        if (app.scheduledStartAt().isAfter(now)) {
            throw SchedulingFailure.invalid("No se puede registrar atención en citas futuras (RF-17).");
        }

        AppointmentStatus completed = catalogs.findStatusByCode(AppointmentStatus.COMPLETED)
                .orElseThrow(() -> SchedulingFailure.invalid("Estado COMPLETED no configurado."));

        Appointment updated = appointments.save(new Appointment(
                app.id(), app.patientUserId(), app.professionalId(), app.locationId(), app.specialtyId(),
                completed.id(), app.scheduledStartAt(), app.scheduledEndAt(), app.rejectionReason(),
                app.createdAt(), null));

        histories.record(new AppointmentStatusHistory(
                null, app.id(), completed.id(), professionalUserId, "PROFESSIONAL", null, "Atención completada"));

        return updated;
    }

    public Appointment noShowAppointment(Long appointmentId, Long professionalUserId, String reason) {
        Appointment app = appointments.findById(appointmentId)
                .orElseThrow(() -> SchedulingFailure.notFound("Cita no encontrada."));

        Professional prof = getProfessionalByUserId(professionalUserId);
        if (!app.professionalId().equals(prof.id())) {
            throw SchedulingFailure.forbidden("Solo el profesional asignado puede marcar inasistencia.");
        }

        AppointmentStatus currentStatus = catalogs.findStatusById(app.statusId())
                .orElseThrow(() -> SchedulingFailure.invalid("Estado actual inválido."));
        if (!AppointmentStatus.APPROVED.equals(currentStatus.code())) {
            throw SchedulingFailure.invalid("Solo se puede registrar inasistencia en citas aprobadas.");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        if (app.scheduledStartAt().isAfter(now)) {
            throw SchedulingFailure.invalid("No se puede registrar inasistencia en citas futuras (RF-17).");
        }

        AppointmentStatus noShow = catalogs.findStatusByCode(AppointmentStatus.NO_SHOW)
                .orElseThrow(() -> SchedulingFailure.invalid("Estado NO_SHOW no configurado."));

        Appointment updated = appointments.save(new Appointment(
                app.id(), app.patientUserId(), app.professionalId(), app.locationId(), app.specialtyId(),
                noShow.id(), app.scheduledStartAt(), app.scheduledEndAt(), app.rejectionReason(),
                app.createdAt(), null));

        histories.record(new AppointmentStatusHistory(
                null, app.id(), noShow.id(), professionalUserId, "PROFESSIONAL", null,
                reason == null || reason.isBlank() ? "Paciente no se presentó" : reason.strip()));

        return updated;
    }

    public List<Appointment> getMyAppointments(Long patientUserId) {
        return appointments.findByPatientUserId(patientUserId);
    }

    public List<Appointment> getProfessionalAppointments(Long professionalUserId, LocalDate date) {
        Professional prof = getProfessionalByUserId(professionalUserId);
        AppointmentStatus approved = catalogs.findStatusByCode(AppointmentStatus.APPROVED)
                .orElseThrow(() -> SchedulingFailure.invalid("Estado APPROVED no configurado."));
        return appointments.findByProfessionalId(prof.id(), date, approved.id());
    }

    public List<Appointment> getAdminAppointments(Short statusId, Short locationId, Long professionalId, LocalDate date) {
        return appointments.findByFilters(statusId, locationId, professionalId, date);
    }

    public List<AppointmentStatusHistory> getAppointmentHistory(Long appointmentId, Long userId, boolean isAdmin) {
        Appointment app = appointments.findById(appointmentId)
                .orElseThrow(() -> SchedulingFailure.notFound("Cita no encontrada."));

        if (!isAdmin && !app.patientUserId().equals(userId)) {
            boolean isAssigned = professionals.findByUserId(userId)
                    .map(p -> p.id().equals(app.professionalId()))
                    .orElse(false);
            if (!isAssigned) {
                throw SchedulingFailure.forbidden("No tienes autorización para consultar el historial de esta cita.");
            }
        }

        return histories.findByAppointmentId(appointmentId);
    }

    // --- Afiliaciones de Pacientes (RF-04) ---
    public record CreateAffiliationCommand(Long userId, Short epsId, Short epsPlanId, Short regimenId) {}

    public List<UserAffiliation> getUserAffiliations(Long userId) {
        if (affiliations == null) return List.of();
        return affiliations.findByUserId(userId);
    }

    public UserAffiliation createAffiliation(CreateAffiliationCommand cmd) {
        if (affiliations == null) throw SchedulingFailure.invalid("Módulo de afiliación no disponible.");
        var existing = affiliations.findExisting(cmd.userId(), cmd.epsId(), cmd.epsPlanId(), cmd.regimenId());
        if (existing.isPresent() && existing.get().active()) {
            throw SchedulingFailure.conflict("Ya existe una afiliación activa con la misma EPS, plan y régimen para este usuario.");
        }
        return affiliations.save(new UserAffiliation(
                null, cmd.userId(), cmd.epsId(), null, cmd.epsPlanId(), null, cmd.regimenId(), null, true, clock.instant()
        ));
    }

    public void deactivateAffiliation(Long affiliationId) {
        if (affiliations != null) {
            affiliations.deactivate(affiliationId);
        }
    }

    // --- Reprogramación de Citas (RF-15, RF-18) ---
    public record RequestRescheduleCommand(Long appointmentId, Long userId, LocalDateTime newStartAt, String reason, boolean isAdmin) {}

    public AppointmentReschedule requestReschedule(RequestRescheduleCommand cmd) {
        if (reschedules == null) throw SchedulingFailure.invalid("Módulo de reprogramación no disponible.");
        Appointment app = appointments.findById(cmd.appointmentId())
                .orElseThrow(() -> SchedulingFailure.notFound("Cita no encontrada."));
        if (!cmd.isAdmin() && !app.patientUserId().equals(cmd.userId())) {
            throw SchedulingFailure.forbidden("No tiene permiso para reprogramar esta cita.");
        }
        if (app.statusId() == 3 || app.statusId() == 4 || app.statusId() == 5 || app.statusId() == 6) {
            throw SchedulingFailure.invalid("No se puede reprogramar una cita en estado terminal.");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        if (cmd.newStartAt().isBefore(now)) {
            throw SchedulingFailure.invalid("La nueva fecha debe ser en el futuro.");
        }
        Specialty specialty = catalogs.findSpecialtyById(app.specialtyId())
                .orElseThrow(() -> SchedulingFailure.notFound("Especialidad no encontrada."));
        LocalDateTime newEndAt = cmd.newStartAt().plusMinutes(specialty.durationMinutes());

        List<ProfessionalSlot> targetSlots = professionalSlots.findSlotsForRange(app.professionalId(), cmd.newStartAt(), newEndAt);
        int requiredSlots = specialty.durationMinutes() / 30;
        if (targetSlots.size() < requiredSlots || targetSlots.stream().anyMatch(s -> !s.isAvailable())) {
            throw SchedulingFailure.conflict("El horario seleccionado para reprogramar no se encuentra disponible.");
        }

        if (cmd.isAdmin()) {
            professionalSlots.releaseSlots(app.id());
            List<Long> slotIds = targetSlots.stream().map(ProfessionalSlot::id).toList();
            professionalSlots.assignSlots(slotIds, app.id());
            appointments.save(new Appointment(app.id(), app.patientUserId(), app.professionalId(),
                    app.locationId(), app.specialtyId(), app.statusId(), cmd.newStartAt(), newEndAt,
                    app.rejectionReason(), app.createdAt(), clock.instant()));
            histories.record(new AppointmentStatusHistory(null, app.id(), app.statusId(), cmd.userId(), "ADMIN", clock.instant(), "Reprogramada por administración: " + cmd.reason()));
            return reschedules.save(new AppointmentReschedule(null, app.id(), cmd.userId(), app.scheduledStartAt(), cmd.newStartAt(), newEndAt, (short) 2, "APPROVED", cmd.reason(), null, clock.instant(), clock.instant()));
        }

        return reschedules.save(new AppointmentReschedule(null, app.id(), cmd.userId(), app.scheduledStartAt(), cmd.newStartAt(), newEndAt, (short) 1, "PENDING", cmd.reason(), null, clock.instant(), clock.instant()));
    }

    public List<AppointmentReschedule> listPendingReschedules() {
        if (reschedules == null) return List.of();
        return reschedules.findByStatusId((short) 1);
    }

    public AppointmentReschedule approveReschedule(Long rescheduleId, Long adminUserId) {
        if (reschedules == null) throw SchedulingFailure.invalid("Módulo de reprogramación no disponible.");
        AppointmentReschedule res = reschedules.findById(rescheduleId)
                .orElseThrow(() -> SchedulingFailure.notFound("Solicitud de reprogramación no encontrada."));
        if (res.statusId() != 1) {
            throw SchedulingFailure.invalid("La solicitud ya fue procesada.");
        }
        Appointment app = appointments.findById(res.appointmentId())
                .orElseThrow(() -> SchedulingFailure.notFound("Cita no encontrada."));
        Specialty specialty = catalogs.findSpecialtyById(app.specialtyId())
                .orElseThrow(() -> SchedulingFailure.notFound("Especialidad no encontrada."));
        List<ProfessionalSlot> targetSlots = professionalSlots.findSlotsForRange(app.professionalId(), res.newStartAt(), res.newEndAt());
        int requiredSlots = specialty.durationMinutes() / 30;
        if (targetSlots.size() < requiredSlots || targetSlots.stream().anyMatch(s -> !s.isAvailable())) {
            throw SchedulingFailure.conflict("Los turnos para reprogramar ya no se encuentran disponibles.");
        }

        professionalSlots.releaseSlots(app.id());
        List<Long> slotIds = targetSlots.stream().map(ProfessionalSlot::id).toList();
        int assigned = professionalSlots.assignSlots(slotIds, app.id());
        if (assigned < slotIds.size()) {
            throw SchedulingFailure.conflict("Conflicto al asignar los nuevos turnos.");
        }

        appointments.save(new Appointment(app.id(), app.patientUserId(), app.professionalId(),
                app.locationId(), app.specialtyId(), (short) 2, res.newStartAt(), res.newEndAt(),
                null, app.createdAt(), clock.instant()));
        histories.record(new AppointmentStatusHistory(null, app.id(), (short) 2, adminUserId, "ADMIN", clock.instant(), "Reprogramación aprobada: " + res.reason()));

        AppointmentReschedule approvedRes = reschedules.save(new AppointmentReschedule(res.id(), res.appointmentId(), res.requestedByUserId(), res.oldStartAt(), res.newStartAt(), res.newEndAt(), (short) 2, "APPROVED", res.reason(), null, res.createdAt(), clock.instant()));
        publishNotification("RESCHEDULE_APPROVED", approvedRes.id(),
                "{\"rescheduleId\":" + approvedRes.id() + ",\"appointmentId\":" + approvedRes.appointmentId() + ",\"newStartAt\":\"" + approvedRes.newStartAt() + "\"}");
        return approvedRes;
    }

    public AppointmentReschedule rejectReschedule(Long rescheduleId, String rejectionReason, Long adminUserId) {
        if (reschedules == null) throw SchedulingFailure.invalid("Módulo de reprogramación no disponible.");
        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw SchedulingFailure.invalid("El motivo de rechazo de la reprogramación es obligatorio.");
        }
        AppointmentReschedule res = reschedules.findById(rescheduleId)
                .orElseThrow(() -> SchedulingFailure.notFound("Solicitud de reprogramación no encontrada."));
        if (res.statusId() != 1) {
            throw SchedulingFailure.invalid("La solicitud ya fue procesada.");
        }
        AppointmentReschedule updated = reschedules.save(new AppointmentReschedule(res.id(), res.appointmentId(), res.requestedByUserId(), res.oldStartAt(), res.newStartAt(), res.newEndAt(), (short) 3, "REJECTED", res.reason(), rejectionReason.strip(), res.createdAt(), clock.instant()));
        publishNotification("RESCHEDULE_REJECTED", updated.id(), "{\"rescheduleId\":" + updated.id() + ",\"appointmentId\":" + updated.appointmentId() + ",\"reason\":\"" + rejectionReason.strip() + "\"}");
        return updated;
    }

    // --- Métodos de Integraciones (S5, S6) ---

    public void publishNotification(String eventType, Long aggregateId, String payload) {
        if (integrations != null) {
            integrations.publishEvent(eventType, aggregateId, payload != null ? payload : "{}");
        }
    }

    public List<UpcomingAppointmentView> getUpcomingAppointments(int hoursAhead) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime to = now.plusHours(hoursAhead > 0 ? hoursAhead : 48);
        return integrations != null ? integrations.findUpcomingApprovedAppointments(now, to) : List.of();
    }

    public AppointmentReminder recordReminder(Long appointmentId, String channel) {
        if (integrations == null) return null;
        return integrations.saveReminder(new AppointmentReminder(
                null, appointmentId, clock.instant(), clock.instant(), "SENT", channel != null ? channel : "EMAIL"
        ));
    }

    public List<NotificationEvent> getPendingEvents() {
        return integrations != null ? integrations.findPendingEvents() : List.of();
    }

    public void markEventProcessed(Long eventId) {
        if (integrations != null) {
            integrations.markEventProcessed(eventId);
        }
    }

    public Map<String, Object> getDailyOperationalSummary(LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now(clock);
        List<Appointment> apps = integrations != null ? integrations.findAppointmentsForDate(targetDate) : List.of();

        Map<Short, String> statusNames = new HashMap<>();
        catalogs.findAllAppointmentStatuses().forEach(s -> statusNames.put(s.id(), s.code()));

        Map<Short, String> locNames = new HashMap<>();
        catalogs.findAllLocations().forEach(l -> locNames.put(l.id(), l.name()));

        Map<String, Long> byStatus = new HashMap<>();
        Map<String, Long> byLocation = new HashMap<>();

        for (Appointment a : apps) {
            String sc = statusNames.getOrDefault(a.statusId(), "UNKNOWN");
            byStatus.put(sc, byStatus.getOrDefault(sc, 0L) + 1);

            String ln = locNames.getOrDefault(a.locationId(), "UNKNOWN");
            byLocation.put(ln, byLocation.getOrDefault(ln, 0L) + 1);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("date", targetDate.toString());
        result.put("totalAppointments", apps.size());
        result.put("byStatus", byStatus);
        result.put("byLocation", byLocation);
        return result;
    }
}

