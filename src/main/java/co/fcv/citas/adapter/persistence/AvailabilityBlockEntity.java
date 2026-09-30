package co.fcv.citas.adapter.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity @Table(name = "availability_blocks")
public class AvailabilityBlockEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "professional_id", nullable = false)
    public Long professionalId;

    @Column(name = "location_id", nullable = false)
    public Short locationId;

    @Column(name = "available_date", nullable = false)
    public LocalDate availableDate;

    @Column(name = "start_time", nullable = false)
    public LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    public LocalTime endTime;

    @Column(nullable = false)
    public boolean active;
}
