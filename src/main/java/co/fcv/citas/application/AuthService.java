package co.fcv.citas.application;

import co.fcv.citas.domain.AuthSession;
import co.fcv.citas.domain.User;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import static co.fcv.citas.application.AuthPorts.*;

// Framework-independent use cases. The inbound facade supplies transaction boundaries.
public final class AuthService {
    private final Users users;
    private final Sessions sessions;
    private final Passwords passwords;
    private final Tokens tokens;
    private final PasswordResetTokens passwordResetTokens;
    private final Clock clock;
    private final Duration refreshLifetime;
    private final String dummyHash;

    public AuthService(Users users, Sessions sessions, Passwords passwords, Tokens tokens,
                       Clock clock, Duration refreshLifetime) {
        this(users, sessions, passwords, tokens, null, clock, refreshLifetime);
    }

    public AuthService(Users users, Sessions sessions, Passwords passwords, Tokens tokens,
                       PasswordResetTokens passwordResetTokens, Clock clock, Duration refreshLifetime) {
        this.users = users; this.sessions = sessions; this.passwords = passwords;
        this.tokens = tokens; this.passwordResetTokens = passwordResetTokens;
        this.clock = clock; this.refreshLifetime = refreshLifetime;
        this.dummyHash = passwords.hash(UUID.randomUUID().toString());
    }

    public record Registration(String firstName, String lastName, String documentType,
                               String documentNumber, String email, String phone, String password) {}
    public record Login(Pair tokens, User user) {}

    public User register(Registration input) {
        int bytes = input.password().getBytes(StandardCharsets.UTF_8).length;
        if (input.password().length() < 8 || bytes > 72) {
            throw new AuthFailure(AuthFailure.Kind.INVALID, "La contraseña requiere mínimo 8 caracteres y máximo 72 bytes UTF-8.");
        }
        return users.create(new User(null, input.firstName().strip(), input.lastName().strip(),
                input.documentType(), input.documentNumber().strip(), normalizeEmail(input.email()),
                input.phone().strip(), passwords.hash(input.password()), true, Set.of("USER")));
    }

    public Login login(String email, String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) throw AuthFailure.unauthorized();
        User user = users.byEmail(normalizeEmail(email)).orElse(null);
        boolean matches = passwords.matches(password, user == null ? dummyHash : user.passwordHash());
        if (!matches || user == null || !user.active()) throw AuthFailure.unauthorized();
        AuthSession session = new AuthSession(UUID.randomUUID().toString(), user.id(),
                UUID.randomUUID().toString(), clock.instant().plus(refreshLifetime), false);
        sessions.create(session);
        return new Login(tokens.issue(user, session), user);
    }

    public Login refresh(String token) {
        Claims claims = tokens.readRefresh(token);
        AuthSession session = sessions.byId(claims.sessionId()).orElseThrow(AuthFailure::unauthorized);
        if (!session.usableAt(clock.instant()) || !session.userId().equals(claims.userId())
                || !session.refreshId().equals(claims.refreshId())) throw AuthFailure.unauthorized();
        User user = users.byId(session.userId()).filter(User::active).orElseThrow(AuthFailure::unauthorized);
        String next = UUID.randomUUID().toString();
        if (!sessions.rotate(session.id(), claims.refreshId(), next, clock.instant())) throw AuthFailure.unauthorized();
        return new Login(tokens.issue(user, new AuthSession(session.id(), user.id(), next,
                session.expiresAt(), false)), user);
    }

    public User identity(Long userId, String sessionId) {
        AuthSession session = sessions.byId(sessionId).orElseThrow(AuthFailure::unauthorized);
        if (!session.userId().equals(userId) || !session.usableAt(clock.instant())) throw AuthFailure.unauthorized();
        return users.byId(userId).filter(User::active).orElseThrow(AuthFailure::unauthorized);
    }

    public void logout(Long userId, String sessionId) { sessions.revoke(sessionId, userId); }

    public record RequestResetResult(String message, String resetToken) {}

    public RequestResetResult requestPasswordReset(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        var optUser = users.byEmail(email);
        if (optUser.isEmpty()) {
            return new RequestResetResult("Si el correo está registrado, se procesará la solicitud.", null);
        }
        User user = optUser.get();
        String rawToken = UUID.randomUUID().toString().replace("-", "");
        String tokenHash = hashToken(rawToken);
        Instant expiresAt = clock.instant().plus(Duration.ofHours(1));
        if (passwordResetTokens != null) {
            passwordResetTokens.save(new co.fcv.citas.domain.PasswordResetToken(null, user.id(), tokenHash, expiresAt, null, clock.instant()));
        }
        return new RequestResetResult("Si el correo está registrado, se procesará la solicitud.", rawToken);
    }

    public void confirmPasswordReset(String rawToken, String newPassword) {
        int bytes = newPassword.getBytes(StandardCharsets.UTF_8).length;
        if (newPassword.length() < 8 || bytes > 72) {
            throw new AuthFailure(AuthFailure.Kind.INVALID, "La contraseña requiere mínimo 8 caracteres y máximo 72 bytes UTF-8.");
        }
        if (passwordResetTokens == null) {
            throw new AuthFailure(AuthFailure.Kind.INVALID, "Servicio de recuperación no disponible.");
        }
        String tokenHash = hashToken(rawToken);
        var token = passwordResetTokens.byTokenHash(tokenHash)
                .orElseThrow(() -> new AuthFailure(AuthFailure.Kind.INVALID, "Token de recuperación inválido o inexistente."));

        if (!token.isValid(clock.instant())) {
            throw new AuthFailure(AuthFailure.Kind.INVALID, "El token de recuperación ha expirado o ya fue utilizado.");
        }

        String newHash = passwords.hash(newPassword);
        users.updatePasswordHash(token.userId(), newHash);
        passwordResetTokens.markUsed(token.id(), clock.instant());
        sessions.revokeAllForUser(token.userId());
    }

    public record UpdateProfileCommand(String firstName, String lastName, String phone) {}

    public User updateProfile(Long userId, UpdateProfileCommand cmd) {
        User existing = users.byId(userId).orElseThrow(AuthFailure::unauthorized);
        User updated = new User(
                existing.id(),
                cmd.firstName() != null ? cmd.firstName().strip() : existing.firstName(),
                cmd.lastName() != null ? cmd.lastName().strip() : existing.lastName(),
                existing.documentType(),
                existing.documentNumber(),
                existing.email(),
                cmd.phone() != null ? cmd.phone().strip() : existing.phone(),
                existing.passwordHash(),
                existing.active(),
                existing.roles()
        );
        return users.update(updated);
    }

    public User getUserProfile(Long userId) {
        return users.byId(userId).filter(User::active).orElseThrow(AuthFailure::unauthorized);
    }

    private static String hashToken(String raw) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String normalizeEmail(String email) { return email.strip().toLowerCase(Locale.ROOT); }
}
