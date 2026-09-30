package co.fcv.citas.adapter.web;

import co.fcv.citas.application.SchedulingPorts;
import co.fcv.citas.application.SchedulingService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchedulingConfiguration {
    @Bean
    public SchedulingService schedulingService(
            SchedulingPorts.Catalogs catalogs,
            SchedulingPorts.Professionals professionals,
            SchedulingPorts.AvailabilityBlocks blocks,
            SchedulingPorts.ProfessionalSlots slots,
            SchedulingPorts.Appointments appointments,
            SchedulingPorts.AppointmentHistories histories,
            co.fcv.citas.application.AuthPorts.Users users,
            co.fcv.citas.application.AuthPorts.Passwords passwords,
            Clock clock) {
        return new SchedulingService(catalogs, professionals, blocks, slots, appointments, histories, users, passwords, clock);
    }
}
