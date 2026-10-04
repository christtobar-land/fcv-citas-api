package co.com.fcv.training.citas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de la integración con n8n. Todos los valores vienen de variables de entorno;
 * ninguna credencial de n8n ni de Google vive en este repositorio.
 */
@ConfigurationProperties(prefix = "app.n8n")
public record N8nProperties(
        boolean enabled,
        String webhookUrl,
        String webhookSecret,
        String serviceToken,
        int timeoutSeconds,
        int maxAttempts
) {
    public boolean webhookActive() {
        return enabled && webhookUrl != null && !webhookUrl.isBlank();
    }

    public boolean serviceTokenActive() {
        return enabled && serviceToken != null && serviceToken.length() >= 16;
    }
}
