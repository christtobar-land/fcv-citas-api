package co.com.fcv.training.citas.application;

import co.com.fcv.training.citas.domain.Account;
import co.com.fcv.training.citas.domain.Identity;
import co.com.fcv.training.citas.domain.RefreshSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Set;

public class AuthService {
    public record Registration(String firstName, String lastName, String documentType,
                               String documentNumber, String email, String phone, String password) {}
    public record Tokens(String accessToken, String refreshToken, long expiresIn) {}
    public record PasswordResetResult(String message, String devToken) {}

    private final Ports.Accounts accounts;
    private final Ports.Sessions sessions;
    private final Ports.Passwords passwords;
    private final Ports.Tokens tokens;
    private final Ports.Transactions transactions;
    private final Ports.PasswordResets passwordResets;
    private final Clock clock;

    public AuthService(Ports.Accounts accounts, Ports.Sessions sessions, Ports.Passwords passwords,
                       Ports.Tokens tokens, Ports.Transactions transactions, Clock clock) {
        this(accounts, sessions, passwords, tokens, transactions, null, clock);
    }

    public AuthService(Ports.Accounts accounts, Ports.Sessions sessions, Ports.Passwords passwords,
                       Ports.Tokens tokens, Ports.Transactions transactions,
                       Ports.PasswordResets passwordResets, Clock clock) {
        this.accounts = accounts;
        this.sessions = sessions;
        this.passwords = passwords;
        this.tokens = tokens;
        this.transactions = transactions;
        this.passwordResets = passwordResets;
        this.clock = clock;
    }

    public Account register(Registration input) {
        return transactions.run(() -> {
            String email = Identity.email(input.email());
            String type = Identity.documentType(input.documentType());
            String number = Identity.required(input.documentNumber());
            if (accounts.existsEmail(email) || accounts.existsDocument(type, number)) throw new DuplicateIdentity();
            String password = input.password();
            if (password == null || password.isBlank()) throw new IllegalArgumentException("Contraseña obligatoria");
            if (password.getBytes(StandardCharsets.UTF_8).length > 72) throw new IllegalArgumentException("Contraseña demasiado larga");
            Account account = new Account(null, Identity.required(input.firstName()),
                    Identity.required(input.lastName()), type, number, email,
                    Identity.required(input.phone()), passwords.hash(password), Set.of("USER"));
            return accounts.save(account);
        });
    }

    public Tokens login(String email, String password) {
        return transactions.run(() -> {
            if (password == null || password.getBytes(StandardCharsets.UTF_8).length > 72) throw new AuthFailure();
            Account account = accounts.byEmail(Identity.email(email)).orElseThrow(AuthFailure::new);
            if (!passwords.matches(password, account.passwordHash())) throw new AuthFailure();
            return issue(account);
        });
    }

    public Tokens refresh(String rawToken) {
        return transactions.run(() -> {
            Ports.RefreshIdentity identity = tokens.readRefresh(rawToken);
            RefreshSession session = sessions.lockByJtiHash(hash(identity.jti())).orElseThrow(AuthFailure::new);
            if (!session.userId().equals(identity.userId()) || !session.activeAt(clock.instant())) throw new AuthFailure();
            Account account = accounts.byId(identity.userId()).orElseThrow(AuthFailure::new);
            sessions.revoke(session.id(), clock.instant());
            return issue(account);
        });
    }

    public void logout(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return;
        try {
            transactions.run(() -> {
                Ports.RefreshIdentity identity = tokens.readRefresh(rawToken);
                sessions.lockByJtiHash(hash(identity.jti()))
                        .filter(s -> s.userId().equals(identity.userId()) && s.activeAt(clock.instant()))
                        .ifPresent(s -> sessions.revoke(s.id(), clock.instant()));
                return null;
            });
        } catch (AuthFailure ignored) {
            // Logout remains idempotent and the controller clears the cookie.
        }
    }

    public PasswordResetResult requestPasswordReset(String rawEmail) {
        if (passwordResets == null) {
            throw new IllegalStateException("Servicio de recuperación no configurado");
        }
        return transactions.run(() -> {
            String email = Identity.email(rawEmail);
            Optional<Account> opt = accounts.byEmail(email);
            if (opt.isEmpty()) {
                return new PasswordResetResult("Si el correo electrónico está registrado, recibirás un código de recuperación.", null);
            }
            Account account = opt.get();
            String plainCode = String.format("%06d", new java.security.SecureRandom().nextInt(1_000_000));
            String tokenHash = hash(plainCode);
            java.time.LocalDateTime expiresAt = java.time.LocalDateTime.now().plusMinutes(15);
            passwordResets.save(account.id(), tokenHash, expiresAt);
            System.out.println("[AUTH] Código de recuperación de contraseña para " + email + ": " + plainCode);
            return new PasswordResetResult("Si el correo electrónico está registrado, recibirás un código de recuperación.", plainCode);
        });
    }

    public void resetPassword(String rawToken, String newPassword) {
        if (passwordResets == null) {
            throw new IllegalStateException("Servicio de recuperación no configurado");
        }
        transactions.run(() -> {
            if (rawToken == null || rawToken.isBlank()) {
                throw new IllegalArgumentException("El código de recuperación es obligatorio");
            }
            if (newPassword == null || newPassword.isBlank()) {
                throw new IllegalArgumentException("La nueva contraseña es obligatoria");
            }
            if (newPassword.getBytes(StandardCharsets.UTF_8).length > 72 || newPassword.length() < 6) {
                throw new IllegalArgumentException("La contraseña debe tener entre 6 y 72 caracteres");
            }

            String tokenHash = hash(rawToken.trim());
            Ports.PasswordResetRecord record = passwordResets.byTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("El código de recuperación es inválido o ha expirado"));

            if (record.usedAt() != null) {
                throw new IllegalArgumentException("El código de recuperación ya fue utilizado");
            }
            if (record.expiresAt().isBefore(java.time.LocalDateTime.now())) {
                throw new IllegalArgumentException("El código de recuperación ha expirado");
            }

            Account account = accounts.byId(record.userId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            Account updatedAccount = new Account(
                account.id(),
                account.firstName(),
                account.lastName(),
                account.documentType(),
                account.documentNumber(),
                account.email(),
                account.phone(),
                passwords.hash(newPassword),
                account.roles()
            );
            accounts.save(updatedAccount);
            passwordResets.markUsed(record.id(), java.time.LocalDateTime.now());
            return null;
        });
    }

    private Tokens issue(Account account) {
        Ports.IssuedRefresh refresh = tokens.refresh(account.id());
        sessions.save(new RefreshSession(null, account.id(), hash(refresh.jti()), refresh.expiresAt(), null));
        return new Tokens(tokens.access(account.id(), account.roles()), refresh.value(), tokens.accessSeconds());
    }

    static String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
