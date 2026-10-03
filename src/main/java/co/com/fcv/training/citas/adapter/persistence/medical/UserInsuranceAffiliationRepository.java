package co.com.fcv.training.citas.adapter.persistence.medical;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserInsuranceAffiliationRepository extends JpaRepository<UserInsuranceAffiliationEntity, Long> {
    Optional<UserInsuranceAffiliationEntity> findByUserIdAndIsCurrentTrue(Long userId);
}
