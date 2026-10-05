package co.fcv.citas;

import co.fcv.citas.adapter.web.IntegrationController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationEndpointsTest {

    @Autowired
    private TestRestTemplate rest;

    private static final String INTEGRATION_KEY = "fcv-n8n-integration-secret-key-2026";

    @Test
    @DisplayName("Endpoints de integración rechazan peticiones sin clave o con clave incorrecta")
    void rejectsUnauthorizedRequests() {
        ResponseEntity<Map> resNoKey = rest.getForEntity("/api/integrations/appointments/upcoming", Map.class);
        assertThat(resNoKey.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        HttpHeaders badHeaders = new HttpHeaders();
        badHeaders.set("X-Integration-Key", "wrong-key");
        HttpEntity<Void> badEntity = new HttpEntity<>(badHeaders);
        ResponseEntity<Map> resBadKey = rest.exchange("/api/integrations/appointments/upcoming", HttpMethod.GET, badEntity, Map.class);
        assertThat(resBadKey.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Flujo end-to-end de integraciones n8n: upcoming, reminders, outbox events, ack y daily summary")
    void fullIntegrationsWorkflow() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Integration-Key", INTEGRATION_KEY);
        HttpEntity<Void> authEntity = new HttpEntity<>(headers);

        // 1. Consultar próximas citas aprobadas (Upcoming) con ventana amplia de horas
        ResponseEntity<List> upcomingRes = rest.exchange(
                "/api/integrations/appointments/upcoming?hoursAhead=8760",
                HttpMethod.GET,
                authEntity,
                List.class
        );
        assertThat(upcomingRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(upcomingRes.getBody()).isNotNull();

        // 2. Registrar recordatorio para cita aprobada existente (ID 128 de V3 seed)
        IntegrationController.RecordReminderRequest reminderReq = new IntegrationController.RecordReminderRequest(128L, "EMAIL");
        HttpEntity<IntegrationController.RecordReminderRequest> postReminderEntity = new HttpEntity<>(reminderReq, headers);
        ResponseEntity<Map> reminderRes = rest.exchange(
                "/api/integrations/appointments/reminders",
                HttpMethod.POST,
                postReminderEntity,
                Map.class
        );
        assertThat(reminderRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(reminderRes.getBody()).containsKey("id");
        assertThat(reminderRes.getBody().get("status")).isEqualTo("SENT");

        // 3. Consultar eventos pendientes en el Outbox
        ResponseEntity<List> eventsRes = rest.exchange(
                "/api/integrations/events/pending",
                HttpMethod.GET,
                authEntity,
                List.class
        );
        assertThat(eventsRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(eventsRes.getBody()).isNotNull();

        // 4. Si hay eventos en el Outbox, hacer acknowledge (ack) del primero
        if (!eventsRes.getBody().isEmpty()) {
            Map firstEvent = (Map) eventsRes.getBody().get(0);
            Number eventId = (Number) firstEvent.get("id");

            ResponseEntity<Map> ackRes = rest.exchange(
                    "/api/integrations/events/" + eventId + "/ack",
                    HttpMethod.POST,
                    authEntity,
                    Map.class
            );
            assertThat(ackRes.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(ackRes.getBody().get("status")).isEqualTo("PROCESSED");
        }

        // 5. Consultar resumen operativo del día (fecha con citas en seed V3)
        ResponseEntity<Map> summaryRes = rest.exchange(
                "/api/integrations/reports/daily-summary?date=2026-10-02",
                HttpMethod.GET,
                authEntity,
                Map.class
        );
        assertThat(summaryRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(summaryRes.getBody()).containsKey("totalAppointments");
        assertThat(summaryRes.getBody()).containsKey("byStatus");
        assertThat(summaryRes.getBody()).containsKey("byLocation");
    }
}
