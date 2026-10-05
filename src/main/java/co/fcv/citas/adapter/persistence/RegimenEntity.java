package co.fcv.citas.adapter.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "regimens")
public class RegimenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Short id;

    @Column(nullable = false, unique = true, length = 30)
    public String code;

    @Column(nullable = false, length = 100)
    public String name;

    @Column(nullable = false)
    public boolean active = true;
}
