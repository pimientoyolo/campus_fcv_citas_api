package co.fcv.citas.adapter.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "professional_slots")
public class ProfessionalSlotEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "availability_block_id", nullable = false)
    public Long availabilityBlockId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "availability_block_id", insertable = false, updatable = false)
    public AvailabilityBlockEntity availabilityBlock;

    @Column(name = "appointment_id")
    public Long appointmentId;

    @Column(name = "start_at", nullable = false)
    public LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    public LocalDateTime endAt;
}
