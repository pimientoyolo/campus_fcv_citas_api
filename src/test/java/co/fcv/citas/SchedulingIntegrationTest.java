package co.fcv.citas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:citas_sched;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa", "spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.show-sql=true", "logging.level.org.hibernate.orm.jdbc.bind=trace"
})
@AutoConfigureMockMvc
class SchedulingIntegrationTest {
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
    static final String ACCESS = UUID.randomUUID() + UUID.randomUUID().toString();
    static final String REFRESH = UUID.randomUUID() + UUID.randomUUID().toString();

    @DynamicPropertySource
    static void secrets(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.access-secret", () -> ACCESS);
        registry.add("app.jwt.refresh-secret", () -> REFRESH);
    }

    @org.springframework.boot.test.context.TestConfiguration
    static class TestClockConfig {
        @org.springframework.context.annotation.Bean
        @org.springframework.context.annotation.Primary
        java.time.Clock testClock() {
            return java.time.Clock.fixed(java.time.Instant.parse("2026-10-01T07:00:00Z"), java.time.ZoneOffset.UTC);
        }
    }



    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    String loginToken(String email, String password) throws Exception {
        var res = mvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(json.writeValueAsBytes(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(res).path("accessToken").asText();
    }

    @Test
    void testCatalogsArePublic() throws Exception {
        mvc.perform(get("/api/catalogs/locations")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("HIC"))
                .andExpect(jsonPath("$[1].code").value("ICV"));

        mvc.perform(get("/api/catalogs/specialties")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("MED_GENERAL"))
                .andExpect(jsonPath("$[1].code").value("CARDIOLOGIA"));
    }

    @Test
    void testGeneralAppointmentIsAutoApprovedAndCannotBeDoubleBooked() throws Exception {
        String patientToken = loginToken("paciente@fcv.test", "User123*");

        // Doctor Mendoza (id 1, Medicina General, HIC = id 1), slot 2026-10-01 08:00:00
        var bookRequest = Map.of(
                "professionalId", 1,
                "locationId", 1,
                "specialtyId", 1,
                "startAt", "2026-10-01T08:00:00"
        );

        // First booking -> APPROVED (RN-02)
        var res = mvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType("application/json")
                .content(json.writeValueAsBytes(bookRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("statusCode").value("APPROVED"))
                .andReturn().getResponse().getContentAsString();

        JsonNode created = json.readTree(res);
        long appointmentId = created.path("id").asLong();

        // Second booking on same slot -> CONFLICT 409 (RN-01 Anti-double booking)
        mvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType("application/json")
                .content(json.writeValueAsBytes(bookRequest)))
                .andExpect(status().isConflict());

        // Patient can view my-appointments
        mvc.perform(get("/api/appointments/my-appointments")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + appointmentId + ")].statusCode").value("APPROVED"));

        // Patient can cancel appointment (RF-14)
        mvc.perform(patch("/api/appointments/" + appointmentId + "/cancel")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("statusCode").value("CANCELLED"));

        // Slot is released after cancellation (RN-09) -> Booking again should succeed!
        mvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType("application/json")
                .content(json.writeValueAsBytes(bookRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("statusCode").value("APPROVED"));
    }

    @Test
    void testSpecializedAppointmentRequiresAdminApprovalAndRejectionRequiresReason() throws Exception {
        String patientToken = loginToken("paciente@fcv.test", "User123*");
        String adminToken = loginToken("admin@fcv.test", "Admin123*");

        // Dra. Castro (id 2, Cardiología = id 2, HIC = id 1, slot 2026-10-01 14:00:00, 60 mins)
        var bookRequest = Map.of(
                "professionalId", 2,
                "locationId", 1,
                "specialtyId", 2,
                "startAt", "2026-10-01T14:00:00"
        );

        // Specialized booking -> REQUESTED (RN-03)
        var res = mvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType("application/json")
                .content(json.writeValueAsBytes(bookRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("statusCode").value("REQUESTED"))
                .andReturn().getResponse().getContentAsString();

        long appointmentId = json.readTree(res).path("id").asLong();

        // Non-admin cannot approve
        mvc.perform(patch("/api/admin/appointments/" + appointmentId + "/approve")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());

        // Admin rejects without reason -> 400 Bad Request (RN-04)
        mvc.perform(patch("/api/admin/appointments/" + appointmentId + "/reject")
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .content(json.writeValueAsBytes(Map.of("reason", "  "))))
                .andExpect(status().isBadRequest());

        // Admin rejects with reason -> REJECTED and slots released (RN-04, RN-09)
        mvc.perform(patch("/api/admin/appointments/" + appointmentId + "/reject")
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .content(json.writeValueAsBytes(Map.of("reason", "No cumple requisitos de remisión previa"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("statusCode").value("REJECTED"))
                .andExpect(jsonPath("rejectionReason").value("No cumple requisitos de remisión previa"));

        // Verify slot is freed after rejection: book again!
        var res2 = mvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType("application/json")
                .content(json.writeValueAsBytes(bookRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("statusCode").value("REQUESTED"))
                .andReturn().getResponse().getContentAsString();

        long appointmentId2 = json.readTree(res2).path("id").asLong();

        // Admin approves -> APPROVED
        mvc.perform(patch("/api/admin/appointments/" + appointmentId2 + "/approve")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("statusCode").value("APPROVED"));
    }

    @Test
    void testProfessionalManagementAndListing() throws Exception {
        String patientToken = loginToken("paciente@fcv.test", "User123*");
        String adminToken = loginToken("admin@fcv.test", "Admin123*");

        // Professionals can be listed publicly or by any user
        mvc.perform(get("/api/professionals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$[0].firstName").value("Carlos"))
                .andExpect(jsonPath("$[0].professionalCode").value("PROF-GEN-001"));

        // Non-admin cannot register professional
        Map<String, Object> newProf = new HashMap<>();
        newProf.put("firstName", "Alberto");
        newProf.put("lastName", "Gómez");
        newProf.put("documentType", "CC");
        newProf.put("documentNumber", "1098765432");
        newProf.put("email", "alberto.gomez@fcv.test");
        newProf.put("phone", "+57 300 999 8877");
        newProf.put("password", "DocPass123*");
        newProf.put("professionalCode", "PROF-CARDIO-999");
        newProf.put("licenseNumber", "MP-99999-COL");
        newProf.put("specialtyIds", List.of(2));
        newProf.put("locationIds", List.of(1));

        mvc.perform(post("/api/professionals")
                .header("Authorization", "Bearer " + patientToken)
                .contentType("application/json")
                .content(json.writeValueAsBytes(newProf)))
                .andExpect(status().isForbidden());

        // Admin can register professional (RF-07)
        mvc.perform(post("/api/professionals")
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .content(json.writeValueAsBytes(newProf)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("professionalCode").value("PROF-CARDIO-999"))
                .andExpect(jsonPath("firstName").value("Alberto"))
                .andExpect(jsonPath("specialties[0].code").value("CARDIOLOGIA"));

        // Verified in list
        mvc.perform(get("/api/professionals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.professionalCode == 'PROF-CARDIO-999')].firstName").value("Alberto"));
    }
}

