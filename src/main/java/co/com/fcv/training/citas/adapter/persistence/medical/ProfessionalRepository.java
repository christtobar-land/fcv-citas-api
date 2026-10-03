package co.com.fcv.training.citas.adapter.persistence.medical;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessionalRepository extends JpaRepository<ProfessionalEntity, Long> {
    List<ProfessionalEntity> findAllByActiveTrue();
    Optional<ProfessionalEntity> findByUserId(Long userId);

    @Query("SELECT DISTINCT p FROM ProfessionalEntity p " +
           "JOIN p.specialties s " +
           "JOIN p.locations l " +
           "WHERE p.active = true AND s.id = :specialtyId AND l.id = :locationId")
    List<ProfessionalEntity> findBySpecialtyAndLocation(
        @Param("specialtyId") Short specialtyId,
        @Param("locationId") Short locationId
    );
}
