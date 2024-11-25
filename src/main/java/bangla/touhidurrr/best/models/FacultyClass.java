package bangla.touhidurrr.best.models;

public record FacultyClass(
        String courseCode,
        String building,
        String room,

        int dayIdx,
        int periodIdx,
        String period,

        String program,
        int intake,
        String section
) {
}
