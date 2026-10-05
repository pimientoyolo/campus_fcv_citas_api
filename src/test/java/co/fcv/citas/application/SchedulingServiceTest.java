package co.fcv.citas.application;

import co.fcv.citas.domain.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SchedulingServiceTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-01T07:00:00Z"), ZoneOffset.UTC);

    private MockCatalogs catalogs;
    private MockProfessionals professionals;
    private MockAvailabilityBlocks blocks;
    private MockProfessionalSlots slots;
    private MockAppointments appointments;
    private MockHistories histories;
    private MockUsers users;
    private MockPasswords passwords;
    private SchedulingService service;

    @BeforeEach
    void setUp() {
        catalogs = new MockCatalogs();
        professionals = new MockProfessionals();
        blocks = new MockAvailabilityBlocks();
        slots = new MockProfessionalSlots();
        appointments = new MockAppointments();
        histories = new MockHistories();
        users = new MockUsers();
        passwords = new MockPasswords();

        service = new SchedulingService(
                catalogs, professionals, blocks, slots, appointments, histories, users, passwords, FIXED_CLOCK
        );
    }

    @Test
    void cannotCreateBlockInThePast() {
        LocalDate pastDate = LocalDate.of(2026, 9, 30);
        var cmd = new SchedulingService.CreateBlockCommand(1L, (short) 1, pastDate, LocalTime.of(8, 0), LocalTime.of(12, 0));

        assertThatThrownBy(() -> service.createBlock(cmd))
                .isInstanceOf(SchedulingFailure.class)
                .hasMessageContaining("pasado");
    }

    @Test
    void cannotCreateBlockIfProfessionalLacksLocation() {
        LocalDate futureDate = LocalDate.of(2026, 10, 5);
        professionals.prof = new Professional(1L, 101L, "CODE", "LIC", true,
                "Dr", "Test", "dr@fcv.test",
                List.of(new Specialty((short) 1, "GEN", "General", 30, true, false, true)),
                List.of(new Location((short) 1, "HIC", "Hospital", "Dir", true)));

        // Attempting to create block in location 2 (not assigned)
        var cmd = new SchedulingService.CreateBlockCommand(1L, (short) 2, futureDate, LocalTime.of(8, 0), LocalTime.of(12, 0));

        assertThatThrownBy(() -> service.createBlock(cmd))
                .isInstanceOf(SchedulingFailure.class)
                .hasMessageContaining("no tiene asignada la sede");
    }

    @Test
    void createBlockDiscretizesInto30MinuteSlots() {
        LocalDate futureDate = LocalDate.of(2026, 10, 5);
        professionals.prof = new Professional(1L, 101L, "CODE", "LIC", true,
                "Dr", "Test", "dr@fcv.test",
                List.of(new Specialty((short) 1, "GEN", "General", 30, true, false, true)),
                List.of(new Location((short) 1, "HIC", "Hospital", "Dir", true)));

        var cmd = new SchedulingService.CreateBlockCommand(1L, (short) 1, futureDate, LocalTime.of(8, 0), LocalTime.of(10, 0));
        AvailabilityBlock block = service.createBlock(cmd);

        assertThat(block).isNotNull();
        // 2 hours = 4 slots of 30 mins
        assertThat(slots.savedSlots).hasSize(4);
    }

    @Test
    void cannotBookInPast() {
        LocalDateTime past = LocalDateTime.of(2026, 9, 30, 8, 0);
        var cmd = new SchedulingService.BookAppointmentCommand(104L, 1L, (short) 1, (short) 1, past);

        assertThatThrownBy(() -> service.bookAppointment(cmd))
                .isInstanceOf(SchedulingFailure.class)
                .hasMessageContaining("pasado");
    }

    @Test
    void generalMedicineAppointmentIsAutoApproved() {
        LocalDateTime time = LocalDateTime.of(2026, 10, 1, 8, 0);
        professionals.prof = new Professional(1L, 101L, "CODE", "LIC", true,
                "Dr", "Test", "dr@fcv.test",
                List.of(new Specialty((short) 1, "GEN", "General", 30, true, false, true)),
                List.of(new Location((short) 1, "HIC", "Hospital", "Dir", true)));

        slots.rangeSlots = List.of(
                new ProfessionalSlot(1L, 1L, 1L, null, time, time.plusMinutes(30))
        );

        var cmd = new SchedulingService.BookAppointmentCommand(104L, 1L, (short) 1, (short) 1, time);
        Appointment app = service.bookAppointment(cmd);

        assertThat(app).isNotNull();
        assertThat(app.statusId()).isEqualTo((short) 2); // APPROVED
    }

    @Test
    void specializedAppointmentRequiresAdminApproval() {
        LocalDateTime time = LocalDateTime.of(2026, 10, 1, 14, 0);
        professionals.prof = new Professional(2L, 102L, "CODE", "LIC", true,
                "Dr", "Cardio", "cardio@fcv.test",
                List.of(new Specialty((short) 2, "CARDIO", "Cardiología", 60, false, true, true)),
                List.of(new Location((short) 1, "HIC", "Hospital", "Dir", true)));

        slots.rangeSlots = List.of(
                new ProfessionalSlot(10L, 2L, 2L, null, time, time.plusMinutes(30)),
                new ProfessionalSlot(11L, 2L, 2L, null, time.plusMinutes(30), time.plusMinutes(60))
        );

        var cmd = new SchedulingService.BookAppointmentCommand(104L, 2L, (short) 1, (short) 2, time);
        Appointment app = service.bookAppointment(cmd);

        assertThat(app).isNotNull();
        assertThat(app.statusId()).isEqualTo((short) 1); // REQUESTED
    }

    // --- Mocks en memoria ---
    static class MockCatalogs implements SchedulingPorts.Catalogs {
        public List<Location> findAllLocations() { return List.of(); }
        public Optional<Location> findLocationById(Short id) { return Optional.of(new Location(id, "LOC", "Loc", "Dir", true)); }
        public List<Specialty> findAllSpecialties() { return List.of(); }
        public Optional<Specialty> findSpecialtyById(Short id) {
            if (id == 1) return Optional.of(new Specialty((short) 1, "GEN", "Medicina General", 30, true, false, true));
            if (id == 2) return Optional.of(new Specialty((short) 2, "CARDIO", "Cardiología", 60, false, true, true));
            return Optional.empty();
        }
        public List<AppointmentStatus> findAllAppointmentStatuses() { return List.of(); }
        public Optional<AppointmentStatus> findStatusById(Short id) {
            if (id == 1) return Optional.of(new AppointmentStatus((short) 1, "REQUESTED", "Solicitada", false));
            if (id == 2) return Optional.of(new AppointmentStatus((short) 2, "APPROVED", "Aprobada", false));
            return Optional.empty();
        }
        public Optional<AppointmentStatus> findStatusByCode(String code) {
            if ("REQUESTED".equals(code)) return Optional.of(new AppointmentStatus((short) 1, "REQUESTED", "Solicitada", false));
            if ("APPROVED".equals(code)) return Optional.of(new AppointmentStatus((short) 2, "APPROVED", "Aprobada", false));
            return Optional.empty();
        }
    }

    static class MockProfessionals implements SchedulingPorts.Professionals {
        Professional prof;
        public List<Professional> findAll(Boolean active, Short specialtyId, Short locationId) { return List.of(); }
        public Optional<Professional> findById(Long id) { return Optional.ofNullable(prof); }
        public Optional<Professional> findByUserId(Long userId) { return Optional.ofNullable(prof); }
        public Professional create(Long userId, String code, String lic, List<Short> s, List<Short> l) { return null; }
        public Professional save(Professional professional) { return professional; }
    }

    static class MockAvailabilityBlocks implements SchedulingPorts.AvailabilityBlocks {
        public List<AvailabilityBlock> findByProfessional(Long id) { return List.of(); }
        public List<AvailabilityBlock> findByProfessionalAndDate(Long id, LocalDate date) { return List.of(); }
        public Optional<AvailabilityBlock> findById(Long id) { return Optional.empty(); }
        public AvailabilityBlock save(AvailabilityBlock block) { return new AvailabilityBlock(1L, block.professionalId(), block.locationId(), block.availableDate(), block.startTime(), block.endTime(), true); }
        public void delete(Long id) {}
    }

    static class MockProfessionalSlots implements SchedulingPorts.ProfessionalSlots {
        List<ProfessionalSlot> savedSlots = new ArrayList<>();
        List<ProfessionalSlot> rangeSlots = new ArrayList<>();
        public List<ProfessionalSlot> findByBlockId(Long blockId) { return List.of(); }
        public List<ProfessionalSlot> findAvailableSlots(Short loc, Long prof, LocalDate d) { return List.of(); }
        public List<ProfessionalSlot> findSlotsForRange(Long prof, LocalDateTime s, LocalDateTime e) { return rangeSlots; }
        public List<ProfessionalSlot> findSlotsByAppointmentId(Long id) { return List.of(); }
        public void saveAll(List<ProfessionalSlot> s) { savedSlots.addAll(s); }
        public int assignSlots(List<Long> slotIds, Long appointmentId) { return slotIds.size(); }
        public void releaseSlots(Long appointmentId) {}
    }

    static class MockAppointments implements SchedulingPorts.Appointments {
        public Appointment save(Appointment a) { return new Appointment(100L, a.patientUserId(), a.professionalId(), a.locationId(), a.specialtyId(), a.statusId(), a.scheduledStartAt(), a.scheduledEndAt(), a.rejectionReason(), Instant.now(), Instant.now()); }
        public Optional<Appointment> findById(Long id) { return Optional.empty(); }
        public List<Appointment> findByPatientUserId(Long id) { return List.of(); }
        public List<Appointment> findByProfessionalId(Long id, LocalDate d, Short s) { return List.of(); }
        public List<Appointment> findByFilters(Short s, Short l, Long p, LocalDate d) { return List.of(); }
    }

    static class MockHistories implements SchedulingPorts.AppointmentHistories {
        public void record(AppointmentStatusHistory history) {}
        public List<AppointmentStatusHistory> findByAppointmentId(Long id) { return List.of(); }
    }

    static class MockUsers implements AuthPorts.Users {
        public Optional<User> byEmail(String email) { return Optional.empty(); }
        public Optional<User> byId(Long id) { return Optional.empty(); }
        public User create(User user) { return user; }
    }

    static class MockPasswords implements AuthPorts.Passwords {
        public String hash(String raw) { return "hash"; }
        public boolean matches(String raw, String hash) { return true; }
    }
}
