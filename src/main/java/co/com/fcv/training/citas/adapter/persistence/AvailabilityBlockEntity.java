package co.com.fcv.training.citas.adapter.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "availability_blocks")
class AvailabilityBlockEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @Column(name = "professional_id", nullable = false) Long professionalId;
    @Column(name = "location_id", nullable = false) Short locationId;
    @Column(name = "available_date", nullable = false) LocalDate availableDate;
    @Column(name = "start_time", nullable = false) LocalTime startTime;
    @Column(name = "end_time", nullable = false) LocalTime endTime;
    @Column(nullable = false) boolean active;
    protected AvailabilityBlockEntity() {}
}
