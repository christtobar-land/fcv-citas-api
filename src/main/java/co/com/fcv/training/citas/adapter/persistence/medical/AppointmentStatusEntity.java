package co.com.fcv.training.citas.adapter.persistence.medical;

import jakarta.persistence.*;

@Entity
@Table(name = "appointment_statuses")
public class AppointmentStatusEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(name = "is_terminal", nullable = false)
    private Boolean isTerminal = false;

    public Short getId() { return id; }
    public void setId(Short id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Boolean getIsTerminal() { return isTerminal; }
    public void setIsTerminal(Boolean isTerminal) { this.isTerminal = isTerminal; }
}
