package co.fcv.citas.adapter.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "appointment_status_history")
public class AppointmentStatusHistoryEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "appointment_id", nullable = false)
    public Long appointmentId;

    @Column(name = "status_id", nullable = false)
    public Short statusId;

    @Column(name = "changed_by_user_id", nullable = false)
    public Long changedByUserId;

    @Column(name = "change_source", nullable = false, length = 20)
    public String changeSource;

    @Column(name = "changed_at", nullable = false)
    public Instant changedAt;

    @Column(name = "reason", length = 500)
    public String reason;

    @PrePersist
    void onPrePersist() {
        if (changedAt == null) changedAt = Instant.now();
    }
}
