package co.com.fcv.training.citas.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * Autentica a n8n en /api/v1/automation/** con la clave de servicio (X-Api-Key, variable N8N_SERVICE_TOKEN).
 * Concede únicamente ROLE_AUTOMATION. Si la clave no está configurada, la ruta queda cerrada.
 */
class ServiceTokenFilter extends OncePerRequestFilter {
    static final String HEADER = "X-Api-Key";
    private final N8nProperties props;

    ServiceTokenFilter(N8nProperties props) {
        this.props = props;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/automation/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String provided = request.getHeader(HEADER);
        if (props.serviceTokenActive() && provided != null && MessageDigest.isEqual(
                provided.getBytes(StandardCharsets.UTF_8), props.serviceToken().getBytes(StandardCharsets.UTF_8))) {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    "n8n", null, List.of(new SimpleGrantedAuthority("ROLE_AUTOMATION"))));
        }
        chain.doFilter(request, response);
    }
}
