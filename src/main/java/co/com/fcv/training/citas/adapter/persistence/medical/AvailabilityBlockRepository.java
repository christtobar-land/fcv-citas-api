package co.com.fcv.training.citas.adapter.persistence.medical;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AvailabilityBlockRepository extends JpaRepository<AvailabilityBlockEntity, Long> {
    List<AvailabilityBlockEntity> findByLocationIdAndAvailableDateAndActiveTrue(Short locationId, LocalDate availableDate);

    List<AvailabilityBlockEntity> findByProfessionalIdAndActiveTrueOrderByAvailableDateAscStartTimeAsc(Long professionalId);

    @Query("SELECT b FROM AvailabilityBlockEntity b " +
           "WHERE b.professional.id = :professionalId " +
           "AND b.availableDate = :availableDate " +
           "AND b.active = true " +
           "AND (:startTime < b.endTime AND :endTime > b.startTime)")
    List<AvailabilityBlockEntity> findOverlappingBlocks(
        @Param("professionalId") Long professionalId,
        @Param("availableDate") LocalDate availableDate,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );
}

