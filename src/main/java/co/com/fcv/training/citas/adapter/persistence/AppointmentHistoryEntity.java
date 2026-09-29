package co.com.fcv.training.citas.adapter.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "appointment_status_history")
class AppointmentHistoryEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @Column(name = "appointment_id", nullable = false) Long appointmentId;
    @Column(name = "status_id", nullable = false) Short statusId;
    @Column(name = "changed_by_user_id") Long changedByUserId;
    @Column(name = "change_source", nullable = false, length = 20) String changeSource;
    @Column(length = 500) String reason;
    protected AppointmentHistoryEntity() {}
}
