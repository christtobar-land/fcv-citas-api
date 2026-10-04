package co.com.fcv.training.citas.adapter.n8n;

import co.com.fcv.training.citas.application.automation.AppointmentNotificationEvent;
import co.com.fcv.training.citas.config.N8nProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Envía los eventos de cita al webhook de n8n (WF-002) tras confirmar la transacción.
 * Nunca afecta al flujo de negocio: los fallos solo se registran. Inactivo si no hay URL configurada.
 */
@Component
public class N8nWebhookPublisher {
    private static final Logger log = LoggerFactory.getLogger(N8nWebhookPublisher.class);
    static final String SECRET_HEADER = "X-Webhook-Secret";

    private final N8nProperties props;
    private final RestClient client;

    public N8nWebhookPublisher(N8nProperties props) {
        this.props = props;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        Duration timeout = Duration.ofSeconds(Math.max(1, props.timeoutSeconds()));
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        this.client = RestClient.builder().requestFactory(factory).build();
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEvent(AppointmentNotificationEvent event) {
        send(event);
    }

    /** Visible para pruebas. Devuelve true si n8n aceptó el evento. */
    boolean send(AppointmentNotificationEvent event) {
        if (!props.webhookActive()) {
            return false;
        }
        int attempts = Math.max(1, props.maxAttempts());
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                client.post()
                        .uri(props.webhookUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SECRET_HEADER, props.webhookSecret() == null ? "" : props.webhookSecret())
                        .body(event)
                        .retrieve()
                        .toBodilessEntity();
                return true;
            } catch (Exception ex) {
                // Se registra solo el tipo de error: la URL y el secreto nunca se escriben en logs.
                log.warn("n8n webhook fallo evento={} intento={}/{} causa={}",
                        event.eventId(), attempt, attempts, ex.getClass().getSimpleName());
                if (attempt < attempts) {
                    try {
                        Thread.sleep(500L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
        }
        return false;
    }
}
