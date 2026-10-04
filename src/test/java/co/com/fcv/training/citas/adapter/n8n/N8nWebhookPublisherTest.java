package co.com.fcv.training.citas.adapter.n8n;

import co.com.fcv.training.citas.application.automation.AppointmentNotificationEvent;
import co.com.fcv.training.citas.config.N8nProperties;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class N8nWebhookPublisherTest {

    private AppointmentNotificationEvent sampleEvent() {
        return new AppointmentNotificationEvent(
                AppointmentNotificationEvent.newId(),
                AppointmentNotificationEvent.APPROVED,
                OffsetDateTime.now(),
                100L,
                "APPROVED",
                "Juan",
                "juan@example.com",
                "Medicina General",
                "Sede Principal",
                "2026-10-10T10:00:00",
                null
        );
    }

    @Test
    void returnsFalseWhenDisabledOrUrlEmpty() {
        var propsDisabled = new N8nProperties(false, "http://localhost:5678/webhook", "secret", "token", 1, 1);
        var publisher1 = new N8nWebhookPublisher(propsDisabled);
        assertThat(publisher1.send(sampleEvent())).isFalse();

        var propsNoUrl = new N8nProperties(true, "", "secret", "token", 1, 1);
        var publisher2 = new N8nWebhookPublisher(propsNoUrl);
        assertThat(publisher2.send(sampleEvent())).isFalse();

        var propsNullUrl = new N8nProperties(true, null, "secret", "token", 1, 1);
        var publisher3 = new N8nWebhookPublisher(propsNullUrl);
        assertThat(publisher3.send(sampleEvent())).isFalse();
    }

    @Test
    void gracefullyHandlesConnectionFailureWithoutThrowing() {
        // Puerto no accesible -> debe capturar y retornar false sin propagar excepción al llamador
        var props = new N8nProperties(true, "http://127.0.0.1:54321/non-existent-webhook", "test-secret", "token", 1, 1);
        var publisher = new N8nWebhookPublisher(props);

        boolean result = publisher.send(sampleEvent());
        assertThat(result).isFalse();
    }
}
