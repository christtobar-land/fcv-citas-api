package co.com.fcv.training.citas.adapter.persistence.medical;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

@Repository
public interface EpsRepository extends JpaRepository<EpsEntity, Long> {
    List<EpsEntity> findAllByActiveTrueOrderByNameAsc();
    List<EpsEntity> findAllByOrderByNameAsc();
    Optional<EpsEntity> findByCode(String code);
}
