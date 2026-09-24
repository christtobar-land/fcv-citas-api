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

interface AppointmentStatusesJpa extends JpaRepository<AppointmentStatusEntity, Short> {}
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
}
