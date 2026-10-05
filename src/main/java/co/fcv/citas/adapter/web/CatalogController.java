package co.fcv.citas.adapter.web;

import co.fcv.citas.domain.AppointmentStatus;
import co.fcv.citas.domain.Location;
import co.fcv.citas.domain.Specialty;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalogs")
public class CatalogController {
    private final SchedulingFacade facade;

    public CatalogController(SchedulingFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/locations")
    public ResponseEntity<List<Location>> getLocations() {
        return ResponseEntity.ok(facade.getLocations());
    }

    @GetMapping("/specialties")
    public ResponseEntity<List<Specialty>> getSpecialties() {
        return ResponseEntity.ok(facade.getSpecialties());
    }

    @GetMapping("/appointment-statuses")
    public ResponseEntity<List<AppointmentStatus>> getStatuses() {
        return ResponseEntity.ok(facade.getAppointmentStatuses());
    }
}
