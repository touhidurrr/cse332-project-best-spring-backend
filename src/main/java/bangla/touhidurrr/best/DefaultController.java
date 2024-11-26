package bangla.touhidurrr.best;

import bangla.touhidurrr.best.models.CourseInfo;
import bangla.touhidurrr.best.models.FacultyInfo;
import bangla.touhidurrr.best.models.Routine;
import bangla.touhidurrr.best.models.RoutineClass;
import bangla.touhidurrr.best.runtime.RoutineInfoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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

    @GetMapping("/courseCodes")
    List<String> getCourseCodes() {
        return routineInfoRepository.getCourseCodes();
    }

    @GetMapping("/courses/{courseCode}")
    CourseInfo getCourseInfo(@PathVariable String courseCode) {
        return routineInfoRepository.getCourseInfo(courseCode);
    }

    @GetMapping("/buildings")
    List<String> getBuildings() {
        return routineInfoRepository.getBuildings();
    }

    @GetMapping("/buildings/{building}")
    List<String> getBuildingInfo(@PathVariable String building) {
        return routineInfoRepository.getRooms(building);
    }

    @GetMapping("/buildingRoomsMap")
    HashMap<String, List<String>> getBuildingRoomsMap() {
        return routineInfoRepository.getBuildingRoomsMap();
    }

    @GetMapping("/routineClasses")
    List<RoutineClass> getClasses(
            @RequestParam("building") Optional<String> building,
            @RequestParam("room") Optional<String> room
    ) {
        if (building.isPresent() && (building.get().isEmpty() || building.get().equals("null")))
            building = Optional.empty();
        if (room.isPresent() && (room.get().isEmpty() || room.get().equals("null"))) room = Optional.empty();

        if (building.isEmpty() && room.isEmpty()) {
            return routineInfoRepository.getClasses();
        }

        if (room.isPresent()) {
            return routineInfoRepository.getClassesInABuildingAndRoom(building.get(), room.get());
        }

        return routineInfoRepository.getClassesInABuilding(building.get());
    }
}
