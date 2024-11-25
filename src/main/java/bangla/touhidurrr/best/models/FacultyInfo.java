package bangla.touhidurrr.best.models;

import java.util.List;

public record FacultyInfo(
        String code,
        String name,
        List<FacultyClass> classes
) {
}
