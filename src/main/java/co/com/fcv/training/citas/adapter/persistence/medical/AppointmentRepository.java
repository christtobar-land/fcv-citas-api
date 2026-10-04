package co.com.fcv.training.citas.adapter.persistence.medical;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {

    @Query("SELECT a FROM AppointmentEntity a " +
           "WHERE a.patient.id = :patientUserId " +
           "ORDER BY a.scheduledStartAt DESC")
    List<AppointmentEntity> findByPatientUserIdOrderByScheduledStartAtDesc(
        @Param("patientUserId") Long patientUserId
    );

    @Query("SELECT a FROM AppointmentEntity a " +
           "WHERE a.id = :id AND a.patient.id = :patientUserId")
    Optional<AppointmentEntity> findByIdAndPatientUserId(
        @Param("id") Long id,
        @Param("patientUserId") Long patientUserId
    );

    @Query("SELECT COUNT(a) > 0 FROM AppointmentEntity a " +
           "WHERE a.patient.id = :patientUserId " +
           "AND a.status.isTerminal = false " +
           "AND a.scheduledStartAt < :endAt " +
           "AND a.scheduledEndAt > :startAt")
    boolean existsActiveAppointmentForPatientInRange(
        @Param("patientUserId") Long patientUserId,
        @Param("startAt") java.time.LocalDateTime startAt,
        @Param("endAt") java.time.LocalDateTime endAt
    );

    @Query("SELECT COUNT(a) > 0 FROM AppointmentEntity a " +
           "WHERE a.patient.id = :patientUserId " +
           "AND a.id <> :excludeAppointmentId " +
           "AND a.status.isTerminal = false " +
           "AND a.scheduledStartAt < :endAt " +
           "AND a.scheduledEndAt > :startAt")
    boolean existsActiveAppointmentForPatientInRangeExcluding(
        @Param("patientUserId") Long patientUserId,
        @Param("excludeAppointmentId") Long excludeAppointmentId,
        @Param("startAt") java.time.LocalDateTime startAt,
        @Param("endAt") java.time.LocalDateTime endAt
    );

    @Query("SELECT a FROM AppointmentEntity a " +
           "WHERE a.professional.id = :professionalId " +
           "AND a.status.code NOT IN ('REQUESTED', 'REJECTED') " +
           "AND (:startAt IS NULL OR a.scheduledStartAt >= :startAt) " +
           "AND (:endAt IS NULL OR a.scheduledStartAt <= :endAt) " +
           "ORDER BY a.scheduledStartAt ASC")
    List<AppointmentEntity> findByProfessionalIdAndDateRange(
        @Param("professionalId") Long professionalId,
        @Param("startAt") java.time.LocalDateTime startAt,
        @Param("endAt") java.time.LocalDateTime endAt
    );

    @Query("SELECT a FROM AppointmentEntity a " +
           "WHERE a.professional.id = :professionalId " +
           "AND a.status.code NOT IN ('REQUESTED', 'REJECTED') " +
           "AND (:locationId IS NULL OR a.location.id = :locationId) " +
           "AND (:startAt IS NULL OR a.scheduledStartAt >= :startAt) " +
           "AND (:endAt IS NULL OR a.scheduledStartAt <= :endAt) " +
           "ORDER BY a.scheduledStartAt ASC")
    List<AppointmentEntity> findByProfessionalIdAndDateRangeAndLocation(
        @Param("professionalId") Long professionalId,
        @Param("locationId") Short locationId,
        @Param("startAt") java.time.LocalDateTime startAt,
        @Param("endAt") java.time.LocalDateTime endAt
    );

    @Query("SELECT a FROM AppointmentEntity a " +
           "WHERE a.status.code IN :statusCodes " +
           "ORDER BY a.scheduledStartAt ASC")
    List<AppointmentEntity> findByStatusCodeIn(
        @Param("statusCodes") List<String> statusCodes
    );

    @Query("SELECT a FROM AppointmentEntity a " +
           "WHERE a.status.code = 'APPROVED' " +
           "AND a.scheduledEndAt < :beforeDateTime")
    List<AppointmentEntity> findPastUnclosedAppointments(
        @Param("beforeDateTime") java.time.LocalDateTime beforeDateTime
    );

    @Query("SELECT COUNT(a) FROM AppointmentEntity a WHERE a.location.id = :locationId AND a.status.code = 'PENDING_APPROVAL'")
    long countPendingApprovalByLocationId(@Param("locationId") Short locationId);
}
