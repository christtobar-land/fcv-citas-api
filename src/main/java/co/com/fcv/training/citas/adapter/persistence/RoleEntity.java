package co.com.fcv.training.citas.adapter.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class RoleEntity {
    @Id Short id;
    @Column(nullable = false, length = 30) String code;
    @Column(nullable = false, length = 80) String name;
    public RoleEntity() {}

    public Short getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
}
