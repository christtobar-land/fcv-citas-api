package co.com.fcv.training.citas.adapter.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
class AppointmentEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @Column(name = "patient_user_id", nullable = false) Long patientUserId;
    @Column(name = "professional_id", nullable = false) Long professionalId;
    @Column(name = "location_id", nullable = false) Short locationId;
    @Column(name = "specialty_id", nullable = false) Short specialtyId;
    @Column(name = "insurance_affiliation_id") Long insuranceAffiliationId;
    @Column(name = "status_id", nullable = false) Short statusId;
    @Column(length = 500) String reason;
    @Column(name = "scheduled_start_at", nullable = false) LocalDateTime scheduledStartAt;
    @Column(name = "scheduled_end_at", nullable = false) LocalDateTime scheduledEndAt;
    @Column(name = "created_by_user_id", nullable = false) Long createdByUserId;
    @Column(name = "approved_by_user_id") Long approvedByUserId;
    @Column(name = "approved_at") LocalDateTime approvedAt;
    protected AppointmentEntity() {}
}
