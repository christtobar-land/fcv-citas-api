package co.com.fcv.training.citas.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RolesJpa extends JpaRepository<RoleEntity, Short> {
    Optional<RoleEntity> findByCode(String code);
}
