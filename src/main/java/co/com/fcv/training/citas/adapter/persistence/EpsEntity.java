package co.com.fcv.training.citas.adapter.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "eps")
class EpsEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @Column(nullable = false, length = 30) String code;
    @Column(nullable = false, length = 150) String name;
    @Column(nullable = false) boolean active;
    protected EpsEntity() {}
}
