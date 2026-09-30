package co.fcv.citas.domain;

import java.util.List;

public record Professional(Long id, Long userId, String professionalCode, String licenseNumber,
                           boolean active, String firstName, String lastName, String email,
                           List<Specialty> specialties, List<Location> locations) {
    public Professional {
        specialties = specialties == null ? List.of() : List.copyOf(specialties);
        locations = locations == null ? List.of() : List.copyOf(locations);
    }
}
