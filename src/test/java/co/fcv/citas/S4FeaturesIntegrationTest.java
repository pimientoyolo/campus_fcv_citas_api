package co.fcv.citas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:citas_s4;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa", "spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver"
})
@AutoConfigureMockMvc
class S4FeaturesIntegrationTest {
    static final String ACCESS = UUID.randomUUID() + UUID.randomUUID().toString();
    static final String REFRESH = UUID.randomUUID() + UUID.randomUUID().toString();

    @DynamicPropertySource
    static void secrets(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.access-secret", () -> ACCESS);
        registry.add("app.jwt.refresh-secret", () -> REFRESH);
    }

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-10-01T07:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    String loginToken(String email, String password) throws Exception {
        String resp = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = json.readTree(resp);
        return node.get("accessToken").asText();
    }

    @Test
    void passwordResetFlowWorksEndToEnd() throws Exception {
        // 1. Registrar paciente
        String email = "reset." + UUID.randomUUID().toString().substring(0, 8) + "@example.test";
        String regPayload = json.writeValueAsString(Map.of(
                "firstName", "Paciente", "lastName", "Reset", "documentType", "CC",
                "documentNumber", UUID.randomUUID().toString().substring(0, 15),
                "email", email, "phone", "3001234567", "password", "ClaveInicial123*"
        ));
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(regPayload))
                .andExpect(status().isCreated());

        // 2. Solicitar restablecimiento
        String reqPayload = json.writeValueAsString(Map.of("email", email));
        String reqResp = mvc.perform(post("/api/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON).content(reqPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode reqNode = json.readTree(reqResp);
        String resetToken = reqNode.get("resetToken").asText();
        assertThat(resetToken).isNotBlank();

        // 3. Confirmar con nueva clave
        String confPayload = json.writeValueAsString(Map.of("token", resetToken, "newPassword", "ClaveNueva456*"));
        mvc.perform(post("/api/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON).content(confPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        // 4. Iniciar sesión con clave anterior (debe fallar 401)
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "password", "ClaveInicial123*"))))
                .andExpect(status().isUnauthorized());

        // 5. Iniciar sesión con clave nueva (debe ser exitoso 200)
        String token = loginToken(email, "ClaveNueva456*");
        assertThat(token).isNotBlank();
    }

    @Test
    void profileAndAffiliationManagement() throws Exception {
        String token = loginToken("paciente@fcv.test", "User123*");

        // 1. Obtener perfil
        mvc.perform(get("/api/users/profile").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("paciente@fcv.test"));

        // 2. Actualizar datos de perfil (RF-04)
        String updatePayload = json.writeValueAsString(Map.of(
                "firstName", "Laura Sofia", "lastName", "Martínez", "phone", "+57 300 999 8877"
        ));
        mvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Laura Sofia"));

        // 3. Crear nueva afiliación
        String affPayload = json.writeValueAsString(Map.of(
                "epsId", 2, "epsPlanId", 3, "regimenId", 2
        ));
        String affResp = mvc.perform(post("/api/users/affiliations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(affPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();
        JsonNode affNode = json.readTree(affResp);
        long affId = affNode.get("id").asLong();

        // 4. Intentar duplicar la misma afiliación activa (debe responder 409 CONFLICT)
        mvc.perform(post("/api/users/affiliations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(affPayload))
                .andExpect(status().isConflict());

        // 5. Desactivar afiliación
        mvc.perform(delete("/api/users/affiliations/" + affId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void adminCatalogCrud() throws Exception {
        String adminToken = loginToken("admin@fcv.test", "Admin123*");

        // 1. Crear EPS
        String code = "EPS" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String epsPayload = json.writeValueAsString(Map.of("code", code, "name", "Nueva EPS de Prueba"));
        String epsResp = mvc.perform(post("/api/admin/catalogs/eps")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(epsPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();
        short epsId = (short) json.readTree(epsResp).get("id").asInt();

        // 2. Crear Plan para esa EPS
        String planPayload = json.writeValueAsString(Map.of("code", "PLAN_GOLD", "name", "Plan Gold"));
        mvc.perform(post("/api/admin/catalogs/eps/" + epsId + "/plans")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(planPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Plan Gold"));

        // 3. Crear Especialidad
        String specCode = "SPEC_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String specPayload = json.writeValueAsString(Map.of(
                "code", specCode, "name", "Neurología Avanzada",
                "appointmentDurationMinutes", 30, "isGeneral", false, "requiresAdminApproval", true
        ));
        mvc.perform(post("/api/admin/catalogs/specialties")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(specPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Neurología Avanzada"));
    }

    @Test
    void appointmentRescheduleFlow() throws Exception {
        String patientToken = loginToken("paciente@fcv.test", "User123*");
        String adminToken = loginToken("admin@fcv.test", "Admin123*");

        // 1. Agendar cita inicial (General: auto-aprobada)
        LocalDateTime initialTime = LocalDateTime.of(2026, 10, 1, 8, 30);
        String bookPayload = json.writeValueAsString(Map.of(
                "professionalId", 1, "locationId", 1, "specialtyId", 1, "startAt", initialTime.toString()
        ));
        String bookResp = mvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON).content(bookPayload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long appId = json.readTree(bookResp).get("id").asLong();

        // 2. Paciente solicita reprogramar para las 09:30 del mismo día
        LocalDateTime newTime = LocalDateTime.of(2026, 10, 1, 9, 30);
        String resPayload = json.writeValueAsString(Map.of(
                "newStartAt", newTime.toString(), "reason", "Cruce imprevisto de horario laboral"
        ));
        String resResp = mvc.perform(post("/api/appointments/" + appId + "/reschedule")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON).content(resPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusId").value(1)) // PENDING
                .andReturn().getResponse().getContentAsString();
        long resId = json.readTree(resResp).get("id").asLong();

        // 3. Admin lista pendientes y aprueba reprogramación
        mvc.perform(get("/api/admin/reschedules/pending").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mvc.perform(patch("/api/admin/reschedules/" + resId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusId").value(2)); // APPROVED
    }
}
