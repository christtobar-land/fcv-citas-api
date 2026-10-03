package co.com.fcv.training.citas.adapter.persistence.medical;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, Short> {
    List<LocationEntity> findAllByActiveTrueOrderByNameAsc();
    List<LocationEntity> findAllByOrderByNameAsc();
    Optional<LocationEntity> findByCode(String code);
}
