package co.com.fcv.training.citas.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsersJpa extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByDocumentTypeAndDocumentNumber(String type, String number);
}
