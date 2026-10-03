package co.com.fcv.training.citas.adapter.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @Column(name = "first_name", nullable = false, length = 80) String firstName;
    @Column(name = "last_name", nullable = false, length = 80) String lastName;
    @Column(name = "document_type", nullable = false, length = 20) String documentType;
    @Column(name = "document_number", nullable = false, length = 40) String documentNumber;
    @Column(nullable = false, length = 160) String email;
    @Column(length = 30) String phone;
    @Column(name = "password_hash", nullable = false, length = 255) String passwordHash;
    @Column(name = "created_at", nullable = false) Instant createdAt;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    Set<RoleEntity> roles = new HashSet<>();

    public UserEntity() {}

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getDocumentType() { return documentType; }
    public String getDocumentNumber() { return documentNumber; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Set<RoleEntity> getRoles() { return roles; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setRoles(Set<RoleEntity> roles) { this.roles = roles; }
}
