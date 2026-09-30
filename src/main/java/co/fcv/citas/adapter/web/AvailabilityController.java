package co.fcv.citas.adapter.web;

import co.fcv.citas.application.SchedulingService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {
    private final SchedulingFacade facade;

    public AvailabilityController(SchedulingFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    public ResponseEntity<List<SchedulingService.AvailableSlotDto>> searchSlots(
            @RequestParam(required = false) Short locationId,
            @RequestParam Short specialtyId,
            @RequestParam(required = false) Long professionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(facade.searchAvailableSlots(locationId, specialtyId, professionalId, date));
    }
}
