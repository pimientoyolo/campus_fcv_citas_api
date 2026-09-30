package co.fcv.citas.adapter.persistence;

import jakarta.persistence.*;

@Entity @Table(name = "specialties")
public class SpecialtyEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Short id;

    @Column(nullable = false, length = 50, unique = true)
    public String code;

    @Column(nullable = false, length = 100)
    public String name;

    @Column(name = "appointment_duration_minutes", nullable = false)
    public short appointmentDurationMinutes;

    @Column(name = "is_general", nullable = false)
    public boolean isGeneral;

    @Column(name = "requires_admin_approval", nullable = false)
    public boolean requiresAdminApproval;

    @Column(nullable = false)
    public boolean active;
}
