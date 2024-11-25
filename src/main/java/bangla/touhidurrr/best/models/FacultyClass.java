package bangla.touhidurrr.best.models;

public record FacultyClass(
        String courseCode,
        String building,
        String room,

        int day,
        int period,

        String program,
        int intake,
        String section
) {
}
