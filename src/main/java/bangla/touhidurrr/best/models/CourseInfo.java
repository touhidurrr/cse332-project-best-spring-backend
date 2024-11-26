package bangla.touhidurrr.best.models;

import java.util.List;

public record CourseInfo(
        Course course,
        List<CourseFaculty> courseFaculties
) {
}
