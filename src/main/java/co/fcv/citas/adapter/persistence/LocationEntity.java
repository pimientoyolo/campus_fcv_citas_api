package co.fcv.citas.adapter.persistence;

import jakarta.persistence.*;

@Entity @Table(name = "locations")
public class LocationEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Short id;

    @Column(nullable = false, length = 20, unique = true)
    public String code;

    @Column(nullable = false, length = 150)
    public String name;

    @Column(nullable = false, length = 255)
    public String address;

    @Column(nullable = false)
    public boolean active;
}
