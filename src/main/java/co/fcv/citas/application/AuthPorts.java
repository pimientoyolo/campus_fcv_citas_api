package co.fcv.citas.application;

import co.fcv.citas.domain.AuthSession;
import co.fcv.citas.domain.User;
import java.time.Instant;
import java.util.Optional;

public final class AuthPorts {
    private AuthPorts() {}
    public interface Users {
        Optional<User> byEmail(String email);
        Optional<User> byId(Long id);
        User create(User user);
        User update(User user);
        void updatePasswordHash(Long userId, String passwordHash);
    }
    public interface Sessions {
        void create(AuthSession session);
        Optional<AuthSession> byId(String id);
        boolean rotate(String sessionId, String oldRefreshId, String newRefreshId, Instant now);
        void revoke(String id, Long userId);
        void revokeAllForUser(Long userId);
    }
    public interface PasswordResetTokens {
        void save(co.fcv.citas.domain.PasswordResetToken token);
        Optional<co.fcv.citas.domain.PasswordResetToken> byTokenHash(String tokenHash);
        void markUsed(Long id, Instant usedAt);
    }
    public interface Passwords {
        String hash(String raw);
        boolean matches(String raw, String hash);
    }
    public interface Tokens {
        Pair issue(User user, AuthSession session);
        Claims readRefresh(String token);
    }
    public record Pair(String accessToken, String refreshToken, String tokenType, long expiresIn) {}
    public record Claims(Long userId, String sessionId, String refreshId) {}
}
