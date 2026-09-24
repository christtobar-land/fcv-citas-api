package co.com.fcv.training.citas;

import co.com.fcv.training.citas.adapter.security.JwtTokens;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class OfferIntegrationTest {
    @Container static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");
    private static final String ACCESS_KEY = UUID.randomUUID() + UUID.randomUUID().toString();
    private static final String REFRESH_KEY = UUID.randomUUID() + UUID.randomUUID().toString();

    @DynamicPropertySource static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("app.jwt.access-secret", () -> ACCESS_KEY);
        registry.add("app.jwt.refresh-secret", () -> REFRESH_KEY);
    }

    @Autowired MockMvc mvc;
    @Autowired JwtTokens jwt;
    @Autowired JdbcTemplate jdbc;

    private String token(String role) { return jwt.access(1L, Set.of(role)); }

    @Test
    void adminCanConfigureSpecialtyAndInvalidDurationIsRejected() throws Exception {
        mvc.perform(get("/api/v1/admin/specialties").header("Authorization", "Bearer " + token("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'MEDICINA_GENERAL')].appointmentDurationMinutes").value(30));

        mvc.perform(post("/api/v1/admin/specialties").header("Authorization", "Bearer " + token("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"DERMATOLOGIA_TEST\",\"name\":\"Dermatología de prueba\",\"appointmentDurationMinutes\":45,\"general\":false,\"requiresAdminApproval\":true}"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/v1/admin/specialties").header("Authorization", "Bearer " + token("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"DERMATOLOGIA_TEST\",\"name\":\"Dermatología de prueba\",\"appointmentDurationMinutes\":30,\"general\":false,\"requiresAdminApproval\":true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCreatesProfessionalAndReplacesAssignments() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String response = mvc.perform(post("/api/v1/admin/professionals").header("Authorization", "Bearer " + token("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("{\"firstName\":\"Profesional\",\"lastName\":\"Sintético\",\"documentType\":\"CC\",\"documentNumber\":\"%s\",\"email\":\"professional-%s@example.test\",\"phone\":\"3000000000\",\"temporaryPassword\":\"SyntheticPass123!\",\"professionalCode\":\"PROF-%s\",\"licenseNumber\":\"RM-TEST-%s\"}").formatted(suffix, suffix, suffix, suffix)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.professionalCode").value("PROF-" + suffix))
                .andExpect(jsonPath("$.licenseNumber").value("RM-TEST-" + suffix))
                .andReturn().getResponse().getContentAsString();
        long professionalId = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response).get("id").asLong();
        assertThat(jdbc.queryForObject("select count(*) from professionals where id = ?", Integer.class, professionalId)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from user_roles ur join roles r on r.id=ur.role_id join professionals p on p.user_id=ur.user_id where p.id=? and r.code='PROFESSIONAL'", Integer.class, professionalId)).isEqualTo(1);

        mvc.perform(put("/api/v1/admin/professionals/" + professionalId + "/specialties")
                        .header("Authorization", "Bearer " + token("ADMIN")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"specialtyIds\":[1,11],\"primarySpecialtyId\":11}"))
                .andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("select count(*) from professional_specialties where professional_id=?", Integer.class, professionalId)).isEqualTo(2);
        assertThat(jdbc.queryForObject("select count(*) from professional_specialties where professional_id=? and specialty_id=11 and is_primary=true", Integer.class, professionalId)).isEqualTo(1);

        mvc.perform(put("/api/v1/admin/professionals/" + professionalId + "/locations")
                        .header("Authorization", "Bearer " + token("ADMIN")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locationIds\":[1,2]}"))
                .andExpect(status().isNoContent());
        mvc.perform(patch("/api/v1/admin/professionals/" + professionalId + "/active")
                        .header("Authorization", "Bearer " + token("ADMIN")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("select active from professionals where id=?", Boolean.class, professionalId)).isFalse();
    }
}
