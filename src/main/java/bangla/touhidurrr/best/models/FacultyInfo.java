package bangla.touhidurrr.best.models;

import java.util.List;

public record FacultyInfo(
        Faculty faculty,
        List<FacultyClass> classes
) {
}
