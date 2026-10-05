package co.fcv.citas.domain;

import java.time.Instant;

public record PasswordResetToken(
        Long id,
        Long userId,
        String tokenHash,
        Instant expiresAt,
        Instant usedAt,
        Instant createdAt
) {
    public boolean isValid(Instant now) {
        return usedAt == null && expiresAt.isAfter(now);
    }
}
