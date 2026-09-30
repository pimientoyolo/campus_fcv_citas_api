package co.fcv.citas.adapter.persistence;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "professionals")
public class ProfessionalEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    public Long userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    public UserEntity user;

    @Column(name = "professional_code", nullable = false, length = 50, unique = true)
    public String professionalCode;

    @Column(name = "license_number", nullable = false, length = 50, unique = true)
    public String licenseNumber;

    @Column(nullable = false)
    public boolean active;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "professional_specialties",
        joinColumns = @JoinColumn(name = "professional_id"),
        inverseJoinColumns = @JoinColumn(name = "specialty_id")
    )
    public List<SpecialtyEntity> specialties = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "professional_locations",
        joinColumns = @JoinColumn(name = "professional_id"),
        inverseJoinColumns = @JoinColumn(name = "location_id")
    )
    public List<LocationEntity> locations = new ArrayList<>();
}
