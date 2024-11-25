package bangla.touhidurrr.best.models;

import java.util.List;

public record CourseInfo(
        String code,
        String name,
        List<CourseFaculty> courseFaculties
) {
}
