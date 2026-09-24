package co.com.fcv.training.citas;

import co.com.fcv.training.citas.adapter.security.JwtTokens;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CatalogIntegrationTest {
    @Container static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");
    private static final String ACCESS_KEY = UUID.randomUUID() + UUID.randomUUID().toString();
    private static final String REFRESH_KEY = UUID.randomUUID() + UUID.randomUUID().toString();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("app.jwt.access-secret", () -> ACCESS_KEY);
        registry.add("app.jwt.refresh-secret", () -> REFRESH_KEY);
    }

    @Autowired MockMvc mvc;
    @Autowired JwtTokens jwt;

    private String userToken() { return jwt.access(99L, Set.of("USER")); }

    @Test
    void fixedCatalogsAreSeededAndReadable() throws Exception {
        String token = userToken();
        mvc.perform(get("/api/v1/catalogs/roles").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'USER')].name").value("Usuario"));
        mvc.perform(get("/api/v1/catalogs/appointment-statuses").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'REQUESTED')].terminal").value(false))
                .andExpect(jsonPath("$[?(@.code == 'CANCELLED')].terminal").value(true));
        mvc.perform(get("/api/v1/catalogs/reschedule-statuses").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'PENDING')].terminal").value(false));
        mvc.perform(get("/api/v1/catalogs/insurance-regimes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'PARTICULAR')].name").value("Particular"));
        mvc.perform(get("/api/v1/catalogs/locations").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'HIC')].name").value("Hospital Internacional de Colombia (HIC)"))
                .andExpect(jsonPath("$[?(@.code == 'ICV')].active").value(true));
    }

    @Test
    void catalogsRequireAuthenticationAndAreReadOnly() throws Exception {
        mvc.perform(get("/api/v1/catalogs/locations")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/catalogs/locations").header("Authorization", "Bearer " + userToken())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }
}
