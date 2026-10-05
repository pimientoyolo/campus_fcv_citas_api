package co.fcv.citas.adapter.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointment_reschedules")
public class AppointmentRescheduleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "appointment_id", nullable = false)
    public Long appointmentId;

    @Column(name = "requested_by_user_id", nullable = false)
    public Long requestedByUserId;

    @Column(name = "old_start_at", nullable = false)
    public LocalDateTime oldStartAt;

    @Column(name = "new_start_at", nullable = false)
    public LocalDateTime newStartAt;

    @Column(name = "new_end_at", nullable = false)
    public LocalDateTime newEndAt;

    @Column(name = "status_id", nullable = false)
    public Short statusId;

    @Column(nullable = false, length = 500)
    public String reason;

    @Column(name = "rejection_reason", length = 500)
    public String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt = Instant.now();
}
