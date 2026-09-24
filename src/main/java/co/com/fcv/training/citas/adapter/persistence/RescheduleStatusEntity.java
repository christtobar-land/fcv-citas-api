package co.com.fcv.training.citas.adapter.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reschedule_request_statuses")
class RescheduleStatusEntity {
    @Id Short id;
    @Column(nullable = false, length = 40) String code;
    @Column(nullable = false, length = 80) String name;
    @Column(name = "is_terminal", nullable = false) boolean terminal;
    protected RescheduleStatusEntity() {}
}
