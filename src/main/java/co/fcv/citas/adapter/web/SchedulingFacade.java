package co.fcv.citas.adapter.web;

import co.fcv.citas.application.SchedulingService;
import co.fcv.citas.domain.*;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchedulingFacade {
    private final SchedulingService service;

    public SchedulingFacade(SchedulingService service) {
        this.service = service;
    }

    @Transactional(readOnly = true)
    public List<Location> getLocations() { return service.getLocations(); }

    @Transactional(readOnly = true)
    public List<Specialty> getSpecialties() { return service.getSpecialties(); }

    @Transactional(readOnly = true)
    public List<AppointmentStatus> getAppointmentStatuses() { return service.getAppointmentStatuses(); }

    @Transactional(readOnly = true)
    public List<Professional> listProfessionals(Boolean active, Short specialtyId, Short locationId) {
        return service.listProfessionals(active, specialtyId, locationId);
    }

    @Transactional(readOnly = true)
    public Professional getProfessional(Long id) { return service.getProfessional(id); }

    @Transactional(readOnly = true)
    public Professional getProfessionalByUserId(Long userId) { return service.getProfessionalByUserId(userId); }

    @Transactional
    public Professional createProfessional(SchedulingService.CreateProfessionalCommand cmd) {
        return service.createProfessional(cmd);
    }

    @Transactional
    public AvailabilityBlock createBlock(SchedulingService.CreateBlockCommand cmd) {
        return service.createBlock(cmd);
    }

    @Transactional
    public void deleteBlock(Long blockId, Long userId, boolean isAdmin) {
        service.deleteBlock(blockId, userId, isAdmin);
    }

    @Transactional(readOnly = true)
    public List<AvailabilityBlock> listBlocks(Long professionalId) {
        return service.listBlocks(professionalId);
    }

    @Transactional(readOnly = true)
    public List<SchedulingService.AvailableSlotDto> searchAvailableSlots(Short locationId, Short specialtyId,
                                                                        Long professionalId, LocalDate date) {
        return service.searchAvailableSlots(locationId, specialtyId, professionalId, date);
    }

    @Transactional
    public Appointment bookAppointment(SchedulingService.BookAppointmentCommand cmd) {
        return service.bookAppointment(cmd);
    }

    @Transactional
    public Appointment cancelAppointment(Long appointmentId, Long userId, boolean isAdmin) {
        return service.cancelAppointment(appointmentId, userId, isAdmin);
    }

    @Transactional
    public Appointment approveAppointment(Long appointmentId, Long adminUserId) {
        return service.approveAppointment(appointmentId, adminUserId);
    }

    @Transactional
    public Appointment rejectAppointment(Long appointmentId, Long adminUserId, String reason) {
        return service.rejectAppointment(appointmentId, adminUserId, reason);
    }

    @Transactional
    public Appointment completeAppointment(Long appointmentId, Long professionalUserId) {
        return service.completeAppointment(appointmentId, professionalUserId);
    }

    @Transactional
    public Appointment noShowAppointment(Long appointmentId, Long professionalUserId, String reason) {
        return service.noShowAppointment(appointmentId, professionalUserId, reason);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getMyAppointments(Long patientUserId) {
        return service.getMyAppointments(patientUserId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getProfessionalAppointments(Long professionalUserId, LocalDate date) {
        return service.getProfessionalAppointments(professionalUserId, date);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAdminAppointments(Short statusId, Short locationId, Long professionalId, LocalDate date) {
        return service.getAdminAppointments(statusId, locationId, professionalId, date);
    }

    @Transactional(readOnly = true)
    public List<AppointmentStatusHistory> getAppointmentHistory(Long appointmentId) {
        return service.getAppointmentHistory(appointmentId);
    }
}
