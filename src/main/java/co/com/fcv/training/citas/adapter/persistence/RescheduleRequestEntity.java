package co.com.fcv.training.citas.adapter.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.Instant;

@Entity
@Table(name = "reschedule_requests")
class RescheduleRequestEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @Column(name = "appointment_id", nullable = false) Long appointmentId;
    @Column(name = "requested_location_id", nullable = false) Short requestedLocationId;
    @Column(name = "requested_start_at", nullable = false) LocalDateTime requestedStartAt;
    @Column(name = "requested_end_at", nullable = false) LocalDateTime requestedEndAt;
    @Column(name = "status_id", nullable = false) Short statusId;
    @Column(name = "requested_by_user_id", nullable = false) Long requestedByUserId;
    @Column(name = "decided_by_user_id") Long decidedByUserId;
    @Column(name = "decision_reason", length = 500) String decisionReason;
    @Column(name = "decided_at") LocalDateTime decidedAt;
    @Column(name = "created_at", insertable = false, updatable = false) Instant createdAt;
    protected RescheduleRequestEntity() {}
}
