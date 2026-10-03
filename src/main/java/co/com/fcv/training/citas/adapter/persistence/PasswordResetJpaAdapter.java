package co.com.fcv.training.citas.adapter.persistence;

import co.com.fcv.training.citas.application.Ports;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
class PasswordResetJpaAdapter implements Ports.PasswordResets {
    private final PasswordResetTokensJpa tokensJpa;

    PasswordResetJpaAdapter(PasswordResetTokensJpa tokensJpa) {
        this.tokensJpa = tokensJpa;
    }

    public void save(Long userId, String tokenHash, LocalDateTime expiresAt) {
        PasswordResetTokenEntity entity = new PasswordResetTokenEntity(userId, tokenHash, expiresAt);
        tokensJpa.save(entity);
    }

    public Optional<Ports.PasswordResetRecord> byTokenHash(String tokenHash) {
        return tokensJpa.findByTokenHash(tokenHash)
            .map(e -> new Ports.PasswordResetRecord(e.getId(), e.getUserId(), e.getTokenHash(), e.getExpiresAt(), e.getUsedAt()));
    }

    public void markUsed(Long id, LocalDateTime usedAt) {
        tokensJpa.findById(id).ifPresent(entity -> {
            entity.setUsedAt(usedAt);
            tokensJpa.save(entity);
        });
    }
}
