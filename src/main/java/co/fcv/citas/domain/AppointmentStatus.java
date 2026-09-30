package co.fcv.citas.domain;

public record AppointmentStatus(Short id, String code, String name, boolean isTerminal) {
    public static final String REQUESTED = "REQUESTED";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";
    public static final String CANCELLED = "CANCELLED";
    public static final String COMPLETED = "COMPLETED";
    public static final String NO_SHOW = "NO_SHOW";
}
