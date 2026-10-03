package co.com.fcv.training.citas.adapter.persistence.medical;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "professional_slots")
public class ProfessionalSlotEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "availability_block_id", nullable = false)
    private AvailabilityBlockEntity availabilityBlock;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "appointment_id")
    private Long appointmentId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AvailabilityBlockEntity getAvailabilityBlock() { return availabilityBlock; }
    public void setAvailabilityBlock(AvailabilityBlockEntity availabilityBlock) { this.availabilityBlock = availabilityBlock; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
}
