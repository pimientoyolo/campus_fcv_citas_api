package co.fcv.citas.adapter.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "eps_plans")
public class EpsPlanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Short id;

    @Column(name = "eps_id", nullable = false)
    public Short epsId;

    @Column(nullable = false, length = 50)
    public String code;

    @Column(nullable = false, length = 150)
    public String name;

    @Column(nullable = false)
    public boolean active = true;
}
