package co.fcv.citas;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:citas_concurrency;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa", "spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver"
})
@AutoConfigureMockMvc
class SchedulingConcurrencyTest {
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    static final String ACCESS = UUID.randomUUID().toString() + UUID.randomUUID().toString();
    static final String REFRESH = UUID.randomUUID().toString() + UUID.randomUUID().toString();

    @DynamicPropertySource
    static void secrets(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.access-secret", () -> ACCESS);
        registry.add("app.jwt.refresh-secret", () -> REFRESH);
    }

    @TestConfiguration
    static class TestClockConfig {
        @Bean
        @Primary
        java.time.Clock testClock() {
            return java.time.Clock.fixed(Instant.parse("2026-10-01T07:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    String loginToken(String email, String password) throws Exception {
        var res = mvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(json.writeValueAsBytes(Map.of("email", email, "password", password))))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(res).path("accessToken").asText();
    }

    @Test
    void concurrentBookingAttemptsResultInExactlyOneSuccessAndOneConflict() throws Exception {
        // Two synthetic patients trying to book the exact same general medicine slot
        String token1 = loginToken("paciente@fcv.test", "User123*");

        // Register a second patient for concurrent collision
        String email2 = "paciente2." + UUID.randomUUID().toString().substring(0, 8) + "@fcv.test";
        mvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content(json.writeValueAsBytes(Map.of(
                        "firstName", "Segundo", "lastName", "Paciente",
                        "documentType", "CC", "documentNumber", UUID.randomUUID().toString().substring(0, 10),
                        "email", email2, "phone", "+57 300 999 8877", "password", "Pass123*"
                ))));
        String token2 = loginToken(email2, "Pass123*");

        // The targeted slot: Dr. Mendoza, HIC, Medicina General, 2026-10-01 10:00:00 (slot 5)
        var bookRequest = Map.of(
                "professionalId", 1,
                "locationId", 1,
                "specialtyId", 1,
                "startAt", "2026-10-01T10:00:00"
        );

        int concurrency = 2;
        ExecutorService pool = Executors.newFixedThreadPool(concurrency);
        CyclicBarrier barrier = new CyclicBarrier(concurrency);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        AtomicInteger otherErrors = new AtomicInteger(0);

        List<Callable<Void>> tasks = List.of(
                () -> {
                    barrier.await(5, TimeUnit.SECONDS);
                    int status = mvc.perform(post("/api/appointments")
                            .header("Authorization", "Bearer " + token1)
                            .contentType("application/json")
                            .content(json.writeValueAsBytes(bookRequest)))
                            .andReturn().getResponse().getStatus();
                    if (status == 201) successCount.incrementAndGet();
                    else if (status == 409) conflictCount.incrementAndGet();
                    else otherErrors.incrementAndGet();
                    return null;
                },
                () -> {
                    barrier.await(5, TimeUnit.SECONDS);
                    int status = mvc.perform(post("/api/appointments")
                            .header("Authorization", "Bearer " + token2)
                            .contentType("application/json")
                            .content(json.writeValueAsBytes(bookRequest)))
                            .andReturn().getResponse().getStatus();
                    if (status == 201) successCount.incrementAndGet();
                    else if (status == 409) conflictCount.incrementAndGet();
                    else otherErrors.incrementAndGet();
                    return null;
                }
        );

        List<Future<Void>> futures = pool.invokeAll(tasks);
        for (Future<Void> f : futures) {
            f.get(10, TimeUnit.SECONDS);
        }
        pool.shutdown();

        // Exact RN-01 verification: Exactly one winner (201), exactly one conflict (409)
        assertThat(successCount.get())
                .as("Exactamente una solicitud debe recibir 201 CREATED")
                .isEqualTo(1);
        assertThat(conflictCount.get())
                .as("Exactamente una solicitud debe recibir 409 CONFLICT (RN-01 Anti-doble reserva)")
                .isEqualTo(1);
        assertThat(otherErrors.get()).isEqualTo(0);
    }
}
