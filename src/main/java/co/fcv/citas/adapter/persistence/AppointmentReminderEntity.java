package co.fcv.citas.adapter.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "appointment_reminders")
public class AppointmentReminderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;

    @Column(name = "scheduled_for", nullable = false)
    private Instant scheduledFor;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, length = 20)
    private String channel;

    public AppointmentReminderEntity() {}

    public AppointmentReminderEntity(Long id, Long appointmentId, Instant scheduledFor, Instant sentAt, String status, String channel) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.scheduledFor = scheduledFor != null ? scheduledFor : Instant.now();
        this.sentAt = sentAt;
        this.status = status != null ? status : "PENDING";
        this.channel = channel != null ? channel : "EMAIL";
    }

    public Long getId() { return id; }
    public Long getAppointmentId() { return appointmentId; }
    public Instant getScheduledFor() { return scheduledFor; }
    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getChannel() { return channel; }
}
