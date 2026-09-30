package co.fcv.citas.domain;

public record Specialty(Short id, String code, String name, int durationMinutes,
                        boolean isGeneral, boolean requiresAdminApproval, boolean active) {
}
