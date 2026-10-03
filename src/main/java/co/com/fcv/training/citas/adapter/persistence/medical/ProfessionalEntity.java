package co.com.fcv.training.citas.adapter.persistence.medical;

import co.com.fcv.training.citas.adapter.persistence.UserEntity;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "professionals")
public class ProfessionalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(name = "professional_code", nullable = false, unique = true, length = 40)
    private String professionalCode;

    @Column(name = "license_number", nullable = false, unique = true, length = 80)
    private String licenseNumber;

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "professional_specialties",
        joinColumns = @JoinColumn(name = "professional_id"),
        inverseJoinColumns = @JoinColumn(name = "specialty_id")
    )
    private List<SpecialtyEntity> specialties;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "professional_locations",
        joinColumns = @JoinColumn(name = "professional_id"),
        inverseJoinColumns = @JoinColumn(name = "location_id")
    )
    private List<LocationEntity> locations;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
    public String getProfessionalCode() { return professionalCode; }
    public void setProfessionalCode(String professionalCode) { this.professionalCode = professionalCode; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public List<SpecialtyEntity> getSpecialties() { return specialties; }
    public void setSpecialties(List<SpecialtyEntity> specialties) { this.specialties = specialties; }
    public List<LocationEntity> getLocations() { return locations; }
    public void setLocations(List<LocationEntity> locations) { this.locations = locations; }
}
