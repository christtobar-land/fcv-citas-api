package co.com.fcv.training.citas.adapter.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "professional_slots")
class ProfessionalSlotEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "availability_block_id", nullable = false) AvailabilityBlockEntity block;
    @Column(name = "start_at", nullable = false) LocalDateTime startAt;
    @Column(name = "end_at", nullable = false) LocalDateTime endAt;
    @Column(name = "appointment_id") Long appointmentId;
    protected ProfessionalSlotEntity() {}
}
