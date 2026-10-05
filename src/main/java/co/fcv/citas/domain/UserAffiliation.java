package co.fcv.citas.domain;

import java.time.Instant;

public record UserAffiliation(
        Long id,
        Long userId,
        Short epsId,
        String epsName,
        Short epsPlanId,
        String epsPlanName,
        Short regimenId,
        String regimenName,
        boolean active,
        Instant createdAt
) {}
