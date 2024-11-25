package bangla.touhidurrr.best;

import bangla.touhidurrr.best.models.FacultyInfo;
import bangla.touhidurrr.best.models.Routine;
import bangla.touhidurrr.best.runtime.RoutineInfoRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "https://cse332.touhidur.pro", "https://xn--p5bnk5b4a3k0aof.xn--45be4a8a4an7e.xn--54b7fta0cc"})
public class DefaultController {
    private final RoutineInfoRepository routineInfoRepository;

    DefaultController(RoutineInfoRepository routineInfoRepository) {
        this.routineInfoRepository = routineInfoRepository;
    }

    @GetMapping("/test")
    String test() {
        return "test message";
    }

    @GetMapping("/routines")
    List<Routine> getRoutines() {
        return routineInfoRepository.getRoutines();
    }

    @GetMapping("/facultyCodes")
    List<String> getFacultyCodes() {
        return routineInfoRepository.getFacultyCodes();
    }

    @GetMapping("/facultyName/{facultyCode}")
    String getFacultyName(@PathVariable String facultyCode) {
        return routineInfoRepository.getFacultyName(facultyCode);
    }

    @GetMapping("/faculty/{facultyCode}")
    FacultyInfo getFacultyInfo(@PathVariable String facultyCode) {
        return new FacultyInfo(
                facultyCode,
                routineInfoRepository.getFacultyName(facultyCode),
                routineInfoRepository.getFacultyClasses(facultyCode)
        );
    }
}
