package co.com.fcv.training.citas.adapter.persistence.medical;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppointmentStatusHistoryRepository extends JpaRepository<AppointmentStatusHistoryEntity, Long> {
    List<AppointmentStatusHistoryEntity> findByAppointmentIdOrderByChangedAtAsc(Long appointmentId);
}
