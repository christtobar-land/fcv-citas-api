package co.com.fcv.training.citas.adapter.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "locations")
class LocationEntity {
    @Id Short id;
    @Column(nullable = false, length = 30) String code;
    @Column(nullable = false, length = 180) String name;
    @Column(nullable = false, length = 255) String address;
    @Column(nullable = false, length = 100) String city;
    @Column(nullable = false, length = 100) String department;
    @Column(nullable = false) boolean active;
    protected LocationEntity() {}
}
