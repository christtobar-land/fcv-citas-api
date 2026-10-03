package co.com.fcv.training.citas.adapter.persistence.medical;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialtyRepository extends JpaRepository<SpecialtyEntity, Short> {
    List<SpecialtyEntity> findAllByActiveTrueOrderByNameAsc();
    List<SpecialtyEntity> findAllByOrderByNameAsc();
    Optional<SpecialtyEntity> findByCode(String code);
}
