package co.com.fcv.training.citas.adapter.persistence.medical;

import co.com.fcv.training.citas.adapter.persistence.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointment_status_history")
public class AppointmentStatusHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private AppointmentEntity appointment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id", nullable = false)
    private AppointmentStatusEntity status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    private UserEntity changedBy;

    @Column(name = "change_source", nullable = false, length = 20)
    private String changeSource = "USER";

    @Column(length = 500)
    private String reason;

    @Column(name = "changed_at", insertable = false, updatable = false)
    private LocalDateTime changedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AppointmentEntity getAppointment() { return appointment; }
    public void setAppointment(AppointmentEntity appointment) { this.appointment = appointment; }
    public AppointmentStatusEntity getStatus() { return status; }
    public void setStatus(AppointmentStatusEntity status) { this.status = status; }
    public UserEntity getChangedBy() { return changedBy; }
    public void setChangedBy(UserEntity changedBy) { this.changedBy = changedBy; }
    public String getChangeSource() { return changeSource; }
    public void setChangeSource(String changeSource) { this.changeSource = changeSource; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getChangedAt() { return changedAt; }
}
