package co.fcv.citas.adapter.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "eps_entities")
public class EpsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Short id;

    @Column(nullable = false, unique = true, length = 30)
    public String code;

    @Column(nullable = false, length = 150)
    public String name;

    @Column(nullable = false)
    public boolean active = true;
}
