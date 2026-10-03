package co.com.fcv.training.citas.adapter.persistence.medical;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProfessionalSlotRepository extends JpaRepository<ProfessionalSlotEntity, Long> {

    @Query("SELECT DISTINCT s FROM ProfessionalSlotEntity s " +
           "JOIN FETCH s.availabilityBlock b " +
           "JOIN FETCH b.professional p " +
           "JOIN FETCH p.user u " +
           "JOIN FETCH b.location l " +
           "JOIN p.specialties spec " +
           "WHERE b.active = true " +
           "AND l.active = true " +
           "AND spec.active = true " +
           "AND p.active = true " +
           "AND (:locationId IS NULL OR b.location.id = :locationId) " +
           "AND (:specialtyId IS NULL OR spec.id = :specialtyId) " +
           "AND (:professionalId IS NULL OR b.professional.id = :professionalId) " +
           "AND s.startAt >= :start " +
           "AND s.startAt <= :end " +
           "AND s.appointmentId IS NULL " +
           "ORDER BY s.startAt ASC")
    List<ProfessionalSlotEntity> findAvailableSlots(
        @Param("locationId") Short locationId,
        @Param("specialtyId") Short specialtyId,
        @Param("professionalId") Long professionalId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ProfessionalSlotEntity s " +
           "JOIN s.availabilityBlock b " +
           "WHERE b.professional.id = :professionalId " +
           "AND (:locationId IS NULL OR b.location.id = :locationId) " +
           "AND s.startAt >= :startAt " +
           "AND s.endAt <= :endAt " +
           "AND s.appointmentId IS NULL " +
           "ORDER BY s.startAt ASC")
    List<ProfessionalSlotEntity> lockFreeSlotsForRange(
        @Param("professionalId") Long professionalId,
        @Param("locationId") Short locationId,
        @Param("startAt") LocalDateTime startAt,
        @Param("endAt") LocalDateTime endAt
    );

    List<ProfessionalSlotEntity> findByAppointmentId(Long appointmentId);

    long countByAvailabilityBlockId(Long availabilityBlockId);

    long countByAvailabilityBlockIdAndAppointmentIdIsNotNull(Long availabilityBlockId);

    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM ProfessionalSlotEntity s WHERE s.availabilityBlock.id = :blockId")
    void deleteByAvailabilityBlockId(@Param("blockId") Long blockId);

    @Query("SELECT COUNT(s) FROM ProfessionalSlotEntity s WHERE s.availabilityBlock.location.id = :locationId AND s.startAt >= :start AND s.startAt <= :end")
    long countByLocationIdAndDateRange(
        @Param("locationId") Short locationId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(s) FROM ProfessionalSlotEntity s WHERE s.availabilityBlock.location.id = :locationId AND s.appointmentId IS NOT NULL AND s.startAt >= :start AND s.startAt <= :end")
    long countBookedByLocationIdAndDateRange(
        @Param("locationId") Short locationId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(DISTINCT s.availabilityBlock.professional.id) FROM ProfessionalSlotEntity s WHERE s.availabilityBlock.location.id = :locationId")
    long countDistinctProfessionalsByLocationId(@Param("locationId") Short locationId);
}
