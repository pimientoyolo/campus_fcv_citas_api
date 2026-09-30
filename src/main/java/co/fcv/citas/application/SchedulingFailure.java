package co.fcv.citas.application;

public class SchedulingFailure extends RuntimeException {
    public enum Kind { INVALID, NOT_FOUND, DUPLICATE, CONFLICT, FORBIDDEN, UNAUTHORIZED }
    private final Kind kind;

    public SchedulingFailure(Kind kind, String message) {
        super(message);
        this.kind = kind;
    }

    public Kind kind() { return kind; }

    public static SchedulingFailure notFound(String message) {
        return new SchedulingFailure(Kind.NOT_FOUND, message);
    }

    public static SchedulingFailure invalid(String message) {
        return new SchedulingFailure(Kind.INVALID, message);
    }

    public static SchedulingFailure conflict(String message) {
        return new SchedulingFailure(Kind.CONFLICT, message);
    }

    public static SchedulingFailure forbidden(String message) {
        return new SchedulingFailure(Kind.FORBIDDEN, message);
    }
}
