package co.com.fcv.training.citas;

import co.com.fcv.training.citas.adapter.security.JwtTokens;
import co.com.fcv.training.citas.application.SchedulingConflict;
import co.com.fcv.training.citas.application.SchedulingService;
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

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SchedulingIntegrationTest {
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
    @Autowired SchedulingService scheduling;

    @Test
    void professionalPublishesNonOverlappingSlotsAndOwnsItsCalendar() throws Exception {
        Fixture fixture = fixture(1);
        LocalDate date = LocalDate.now().plusDays(3);
        String payload = "{\"locationId\":1,\"date\":\"%s\",\"startTime\":\"08:00:00\",\"endTime\":\"09:00:00\"}".formatted(date);
        String response = mvc.perform(post("/api/v1/professional/availability-blocks")
                        .header("Authorization", bearer(fixture.professionalUserId(), "PROFESSIONAL"))
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.date").value(date.toString())).andReturn().getResponse().getContentAsString();
        long blockId = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response).get("id").asLong();
        assertThat(jdbc.queryForObject("select count(*) from professional_slots where availability_block_id=?", Integer.class, blockId)).isEqualTo(2);

        mvc.perform(post("/api/v1/professional/availability-blocks")
                        .header("Authorization", bearer(fixture.professionalUserId(), "PROFESSIONAL"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locationId\":1,\"date\":\"%s\",\"startTime\":\"08:30:00\",\"endTime\":\"10:00:00\"}".formatted(date)))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/v1/professional/availability-blocks").header("Authorization", bearer(fixture.professionalUserId(), "PROFESSIONAL"))
                        .param("date", date.toString()).param("locationId", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(blockId));
    }

    @Test
    void generalReservationIsApprovedAndSecondReservationIsRejected() throws Exception {
        Fixture fixture = fixture(1);
        LocalDate date = LocalDate.now().plusDays(4);
        publish(fixture, date, "08:00:00", "09:00:00");
        mvc.perform(get("/api/v1/availability").header("Authorization", bearer(fixture.patientUserId(), "USER"))
                        .param("locationId", "1").param("specialtyId", "1").param("date", date.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].startAt").exists());
        String reservation = "{\"professionalId\":%d,\"locationId\":1,\"specialtyId\":1,\"startAt\":\"%sT08:00:00\"}".formatted(fixture.professionalId(), date);
        String result = mvc.perform(post("/api/v1/appointments").header("Authorization", bearer(fixture.patientUserId(), "USER"))
                        .contentType(MediaType.APPLICATION_JSON).content(reservation))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("APPROVED")).andReturn().getResponse().getContentAsString();
        long appointment = new com.fasterxml.jackson.databind.ObjectMapper().readTree(result).get("id").asLong();
        assertThat(jdbc.queryForObject("select count(*) from professional_slots where appointment_id=?", Integer.class, appointment)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select change_source from appointment_status_history where appointment_id=?", String.class, appointment)).isEqualTo("SYSTEM");
        mvc.perform(post("/api/v1/appointments").header("Authorization", bearer(fixture.secondPatientUserId(), "USER"))
                        .contentType(MediaType.APPLICATION_JSON).content(reservation))
                .andExpect(status().isConflict());
    }

    @Test
    void specializedReservationIsRetainedAndAdminCanRejectAndReleaseIt() throws Exception {
        Fixture fixture = fixture(11);
        LocalDate date = LocalDate.now().plusDays(5);
        publish(fixture, date, "08:00:00", "10:00:00");
        String reservation = "{\"professionalId\":%d,\"locationId\":1,\"specialtyId\":11,\"startAt\":\"%sT08:00:00\",\"reason\":\"Control sintético\"}".formatted(fixture.professionalId(), date);
        String result = mvc.perform(post("/api/v1/appointments").header("Authorization", bearer(fixture.patientUserId(), "USER"))
                        .contentType(MediaType.APPLICATION_JSON).content(reservation))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("REQUESTED")).andReturn().getResponse().getContentAsString();
        long appointment = new com.fasterxml.jackson.databind.ObjectMapper().readTree(result).get("id").asLong();
        assertThat(jdbc.queryForObject("select count(*) from professional_slots where appointment_id=?", Integer.class, appointment)).isEqualTo(2);
        mvc.perform(post("/api/v1/admin/appointments/" + appointment + "/decision").header("Authorization", bearer(fixture.adminUserId(), "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"decision\":\"REJECT\",\"reason\":\"Motivo sintético\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REJECTED"));
        assertThat(jdbc.queryForObject("select count(*) from professional_slots where appointment_id=?", Integer.class, appointment)).isZero();
    }

    @Test
    void concurrentReservationsLeaveExactlyOneAppointmentForTheSameSlot() throws Exception {
        Fixture fixture = fixture(1);
        LocalDate date = LocalDate.now().plusDays(6);
        publish(fixture, date, "08:00:00", "09:00:00");
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var first = pool.submit(() -> reserveConcurrently(fixture.patientUserId(), fixture, date, ready, start));
            var second = pool.submit(() -> reserveConcurrently(fixture.secondPatientUserId(), fixture, date, ready, start));
            ready.await(); start.countDown();
            assertThat(Set.of(first.get(), second.get())).containsExactlyInAnyOrder("reserved", "conflict");
        }
        assertThat(jdbc.queryForObject("select count(*) from appointments where professional_id=? and scheduled_start_at=?", Integer.class,
                fixture.professionalId(), java.sql.Timestamp.valueOf(date.atTime(8, 0)))).isEqualTo(1);
    }

    private String reserveConcurrently(long patientId, Fixture fixture, LocalDate date, CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown(); start.await();
        try {
            scheduling.reserve(patientId, new SchedulingService.Reservation(fixture.professionalId(), (short) 1, (short) 1,
                    date.atTime(8, 0), null));
            return "reserved";
        } catch (SchedulingConflict expected) { return "conflict"; }
    }

    private void publish(Fixture fixture, LocalDate date, String start, String end) throws Exception {
        mvc.perform(post("/api/v1/professional/availability-blocks").header("Authorization", bearer(fixture.professionalUserId(), "PROFESSIONAL"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locationId\":1,\"date\":\"%s\",\"startTime\":\"%s\",\"endTime\":\"%s\"}".formatted(date, start, end)))
                .andExpect(status().isCreated());
    }

    private Fixture fixture(int specialtyId) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        long professionalUser = user("professional-" + suffix + "@example.test", "PROFESSIONAL", suffix + "1");
        jdbc.update("insert into professionals (user_id,professional_code,license_number,active) values (?,?,?,true)",
                professionalUser, "PROF-" + suffix, "LIC-" + suffix);
        long professional = jdbc.queryForObject("select id from professionals where user_id=?", Long.class, professionalUser);
        jdbc.update("insert into professional_specialties (professional_id,specialty_id,is_primary,active) values (?,?,true,true)", professional, specialtyId);
        jdbc.update("insert into professional_locations (professional_id,location_id,active) values (?,1,true)", professional);
        return new Fixture(professionalUser, professional, user("patient-" + suffix + "@example.test", "USER", suffix + "2"),
                user("patient2-" + suffix + "@example.test", "USER", suffix + "3"), user("admin-" + suffix + "@example.test", "ADMIN", suffix + "4"));
    }

    private long user(String email, String role, String suffix) {
        jdbc.update("insert into users (first_name,last_name,document_type,document_number,email,phone,password_hash) values ('Synthetic','User','CC',?,?,?,?)",
                suffix, email, "3000000000", "hash");
        long id = jdbc.queryForObject("select id from users where email=?", Long.class, email);
        short roleId = jdbc.queryForObject("select id from roles where code=?", Short.class, role);
        jdbc.update("insert into user_roles (user_id,role_id) values (?,?)", id, roleId);
        return id;
    }
    private String bearer(long userId, String role) { return "Bearer " + jwt.access(userId, Set.of(role)); }
    private record Fixture(long professionalUserId, long professionalId, long patientUserId, long secondPatientUserId, long adminUserId) {}
}
