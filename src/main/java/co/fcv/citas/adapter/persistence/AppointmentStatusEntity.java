package co.fcv.citas.adapter.persistence;

import jakarta.persistence.*;

@Entity @Table(name = "appointment_statuses")
public class AppointmentStatusEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Short id;

    @Column(nullable = false, length = 30, unique = true)
    public String code;

    @Column(nullable = false, length = 50)
    public String name;

    @Column(name = "is_terminal", nullable = false)
    public boolean isTerminal;
}
