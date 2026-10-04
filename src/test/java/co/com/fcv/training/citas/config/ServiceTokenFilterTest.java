package co.com.fcv.training.citas.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceTokenFilterTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ignoresNonAutomationEndpoints() throws Exception {
        var props = new N8nProperties(true, "http://localhost:5678/webhook", "secret", "valid-token-at-least-16-chars", 5, 3);
        var filter = new ServiceTokenFilter(props);
        var request = new MockHttpServletRequest("GET", "/api/v1/appointments/my");
        request.addHeader("X-Api-Key", "valid-token-at-least-16-chars");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {});

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void authenticatesWhenApiKeyMatches() throws Exception {
        var props = new N8nProperties(true, "http://localhost:5678/webhook", "secret", "valid-token-at-least-16-chars", 5, 3);
        var filter = new ServiceTokenFilter(props);
        var request = new MockHttpServletRequest("GET", "/api/v1/automation/appointments/upcoming");
        request.addHeader("X-Api-Key", "valid-token-at-least-16-chars");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {});

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("n8n");
        assertThat(auth.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_AUTOMATION"));
    }

    @Test
    void doesNotAuthenticateWhenApiKeyIsInvalidOrMissing() throws Exception {
        var props = new N8nProperties(true, "http://localhost:5678/webhook", "secret", "valid-token-at-least-16-chars", 5, 3);
        var filter = new ServiceTokenFilter(props);
        var request = new MockHttpServletRequest("GET", "/api/v1/automation/appointments/upcoming");
        request.addHeader("X-Api-Key", "wrong-token");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {});

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doesNotAuthenticateWhenFeatureIsDisabled() throws Exception {
        var props = new N8nProperties(false, "http://localhost:5678/webhook", "secret", "valid-token-at-least-16-chars", 5, 3);
        var filter = new ServiceTokenFilter(props);
        var request = new MockHttpServletRequest("GET", "/api/v1/automation/appointments/upcoming");
        request.addHeader("X-Api-Key", "valid-token-at-least-16-chars");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {});

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
