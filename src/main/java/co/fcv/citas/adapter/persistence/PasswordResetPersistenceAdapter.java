package co.fcv.citas.adapter.persistence;

import co.fcv.citas.application.AuthPorts;
import co.fcv.citas.domain.PasswordResetToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PasswordResetPersistenceAdapter implements AuthPorts.PasswordResetTokens {
    private final PasswordResetTokenJpaRepository repository;

    public PasswordResetPersistenceAdapter(PasswordResetTokenJpaRepository repository) {
        this.repository = repository;
    }

    public void save(PasswordResetToken token) {
        PasswordResetTokenEntity e = new PasswordResetTokenEntity();
        e.userId = token.userId();
        e.tokenHash = token.tokenHash();
        e.expiresAt = token.expiresAt();
        e.usedAt = token.usedAt();
        e.createdAt = token.createdAt();
        repository.saveAndFlush(e);
    }

    public Optional<PasswordResetToken> byTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(this::map);
    }

    public void markUsed(Long id, Instant usedAt) {
        repository.findById(id).ifPresent(e -> {
            e.usedAt = usedAt;
            repository.saveAndFlush(e);
        });
    }

    private PasswordResetToken map(PasswordResetTokenEntity e) {
        return new PasswordResetToken(e.id, e.userId, e.tokenHash, e.expiresAt, e.usedAt, e.createdAt);
    }
}
