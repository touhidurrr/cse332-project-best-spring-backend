package bangla.touhidurrr.best.models;

public record Routine(
        String program,
        int intake,
        String section,
        String semester,
        String[] periods,
        Class[][] classes
) {
}
