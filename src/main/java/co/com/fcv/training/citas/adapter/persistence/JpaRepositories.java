package co.com.fcv.training.citas.adapter.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

interface UsersJpa extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByDocumentTypeAndDocumentNumber(String type, String number);
}

interface RolesJpa extends JpaRepository<RoleEntity, Short> {
    Optional<RoleEntity> findByCode(String code);
}

interface SessionsJpa extends JpaRepository<SessionEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SessionEntity s where s.tokenHash = :hash")
    Optional<SessionEntity> lockByTokenHash(@Param("hash") String hash);
}

interface AppointmentStatusesJpa extends JpaRepository<AppointmentStatusEntity, Short> {
    Optional<AppointmentStatusEntity> findByCode(String code);
}
interface RescheduleStatusesJpa extends JpaRepository<RescheduleStatusEntity, Short> {}
interface InsuranceRegimesJpa extends JpaRepository<InsuranceRegimeEntity, Short> {}
interface LocationsJpa extends JpaRepository<LocationEntity, Short> {}
interface EpsJpa extends JpaRepository<EpsEntity, Long> {}

interface EpsPlansJpa extends JpaRepository<EpsPlanEntity, Long> {
    @Query("select p from EpsPlanEntity p join fetch p.eps e join fetch p.regime r where p.active = true and e.active = true order by p.id")
    java.util.List<EpsPlanEntity> findAllActive();

    @Query("select p from EpsPlanEntity p join fetch p.eps e join fetch p.regime r where p.id = :id and p.active = true and e.active = true")
    Optional<EpsPlanEntity> findActiveById(@Param("id") Long id);
}

interface AffiliationsJpa extends JpaRepository<UserInsuranceAffiliationEntity, Long> {
    boolean existsByUser_IdAndCurrentTrue(Long userId);
    Optional<UserInsuranceAffiliationEntity> findByUser_IdAndCurrentTrue(Long userId);
}

interface SpecialtiesJpa extends JpaRepository<SpecialtyEntity, Short> {
    java.util.List<SpecialtyEntity> findAllByOrderById();
}

interface ProfessionalsJpa extends JpaRepository<ProfessionalEntity, Long> {
    Optional<ProfessionalEntity> findByUserId(Long userId);
    @Query("select p from ProfessionalEntity p where p.active = true and (:professionalId is null or p.id = :professionalId) and exists (select ps from ProfessionalSpecialtyEntity ps where ps.id.professionalId = p.id and ps.id.specialtyId = :specialtyId and ps.active = true) and exists (select pl from ProfessionalLocationEntity pl where pl.id.professionalId = p.id and pl.id.locationId = :locationId and pl.active = true)")
    java.util.List<ProfessionalEntity> reservable(@Param("specialtyId") Short specialtyId,
                                                  @Param("locationId") Short locationId,
                                                  @Param("professionalId") Long professionalId);
}

interface AvailabilityBlocksJpa extends JpaRepository<AvailabilityBlockEntity, Long> {
    @Query("select b from AvailabilityBlockEntity b where b.professionalId = :professionalId and b.active = true and b.availableDate = :date and b.startTime < :endTime and b.endTime > :startTime and (:excludedId is null or b.id <> :excludedId)")
    java.util.List<AvailabilityBlockEntity> overlaps(@Param("professionalId") Long professionalId,
                                                     @Param("date") java.time.LocalDate date,
                                                     @Param("startTime") java.time.LocalTime startTime,
                                                     @Param("endTime") java.time.LocalTime endTime,
                                                     @Param("excludedId") Long excludedId);
    @Query("select b from AvailabilityBlockEntity b where b.professionalId = :professionalId and b.active = true and (:date is null or b.availableDate = :date) and (:locationId is null or b.locationId = :locationId) order by b.availableDate, b.startTime")
    java.util.List<AvailabilityBlockEntity> ownBlocks(@Param("professionalId") Long professionalId,
                                                       @Param("date") java.time.LocalDate date,
                                                       @Param("locationId") Short locationId);
}

interface ProfessionalSlotsJpa extends JpaRepository<ProfessionalSlotEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ProfessionalSlotEntity s join s.block b where b.professionalId = :professionalId and b.locationId = :locationId and b.active = true and s.startAt in :starts order by s.startAt")
    java.util.List<ProfessionalSlotEntity> lockForReservation(@Param("professionalId") Long professionalId,
                                                              @Param("locationId") Short locationId,
                                                              @Param("starts") java.util.List<java.time.LocalDateTime> starts);
    @Query("select s from ProfessionalSlotEntity s join fetch s.block b where b.professionalId in :professionalIds and b.locationId = :locationId and b.availableDate = :date and b.active = true and s.appointmentId is null and s.rescheduleRequestId is null order by b.professionalId, s.startAt")
    java.util.List<ProfessionalSlotEntity> freeSlots(@Param("professionalIds") java.util.List<Long> professionalIds,
                                                     @Param("locationId") Short locationId,
                                                     @Param("date") java.time.LocalDate date);
    @Query("select count(s) > 0 from ProfessionalSlotEntity s where s.block.id = :blockId and (s.appointmentId is not null or s.rescheduleRequestId is not null)")
    boolean hasCommittedSlots(@Param("blockId") Long blockId);
    java.util.List<ProfessionalSlotEntity> findByBlock_Id(Long blockId);
    java.util.List<ProfessionalSlotEntity> findByAppointmentId(Long appointmentId);
    java.util.List<ProfessionalSlotEntity> findByRescheduleRequestId(Long requestId);
}

interface RescheduleRequestsJpa extends JpaRepository<RescheduleRequestEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RescheduleRequestEntity r where r.id = :id")
    Optional<RescheduleRequestEntity> lockById(@Param("id") Long id);
    @Query("select r from RescheduleRequestEntity r where r.appointmentId = :appointmentId and r.statusId = :statusId")
    java.util.List<RescheduleRequestEntity> findByAppointmentIdAndStatusId(@Param("appointmentId") Long appointmentId, @Param("statusId") Short statusId);
    java.util.List<RescheduleRequestEntity> findByStatusIdOrderByCreatedAtAsc(Short statusId);
}

interface AppointmentsJpa extends JpaRepository<AppointmentEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AppointmentEntity a where a.id = :id")
    Optional<AppointmentEntity> lockById(@Param("id") Long id);
    java.util.List<AppointmentEntity> findByStatusIdOrderByScheduledStartAt(Short statusId);
    @Query("select a from AppointmentEntity a where a.patientUserId=:userId and (:statusId is null or a.statusId=:statusId) and (:date is null or function('date', a.scheduledStartAt)=:date) order by a.scheduledStartAt desc")
    java.util.List<AppointmentEntity> patientAppointments(@Param("userId") Long userId, @Param("statusId") Short statusId, @Param("date") java.time.LocalDate date);
    @Query("select a from AppointmentEntity a where a.professionalId=:professionalId and a.statusId=:statusId and (:date is null or function('date', a.scheduledStartAt)=:date) and (:locationId is null or a.locationId=:locationId) order by a.scheduledStartAt")
    java.util.List<AppointmentEntity> professionalAgenda(@Param("professionalId") Long professionalId, @Param("statusId") Short statusId, @Param("date") java.time.LocalDate date, @Param("locationId") Short locationId);
}

interface AppointmentHistoriesJpa extends JpaRepository<AppointmentHistoryEntity, Long> {
    java.util.List<AppointmentHistoryEntity> findByAppointmentIdOrderByChangedAt(Long appointmentId);
}

interface ProfessionalSpecialtiesJpa extends JpaRepository<ProfessionalSpecialtyEntity, ProfessionalSpecialtyKey> {
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("delete from ProfessionalSpecialtyEntity e where e.id.professionalId = :professionalId")
    void deleteByProfessionalId(@org.springframework.data.repository.query.Param("professionalId") Long professionalId);
}

interface ProfessionalLocationsJpa extends JpaRepository<ProfessionalLocationEntity, ProfessionalLocationKey> {
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("delete from ProfessionalLocationEntity e where e.id.professionalId = :professionalId")
    void deleteByProfessionalId(@org.springframework.data.repository.query.Param("professionalId") Long professionalId);
}
