package co.fcv.citas.adapter.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity @Table(name = "appointments")
public class AppointmentEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "patient_user_id", nullable = false)
    public Long patientUserId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_user_id", insertable = false, updatable = false)
    public UserEntity patient;

    @Column(name = "professional_id", nullable = false)
    public Long professionalId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "professional_id", insertable = false, updatable = false)
    public ProfessionalEntity professional;

    @Column(name = "location_id", nullable = false)
    public Short locationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id", insertable = false, updatable = false)
    public LocationEntity location;

    @Column(name = "specialty_id", nullable = false)
    public Short specialtyId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "specialty_id", insertable = false, updatable = false)
    public SpecialtyEntity specialty;

    @Column(name = "status_id", nullable = false)
    public Short statusId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id", insertable = false, updatable = false)
    public AppointmentStatusEntity status;

    @Column(name = "scheduled_start_at", nullable = false)
    public LocalDateTime scheduledStartAt;

    @Column(name = "scheduled_end_at", nullable = false)
    public LocalDateTime scheduledEndAt;

    @Column(name = "rejection_reason", length = 500)
    public String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @PrePersist
    void onPrePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    void onPreUpdate() {
        updatedAt = Instant.now();
    }
}
