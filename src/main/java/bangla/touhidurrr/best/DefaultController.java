package bangla.touhidurrr.best;

import bangla.touhidurrr.best.models.Routine;
import bangla.touhidurrr.best.runtime.RoutineInfoRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DefaultController {
    private RoutineInfoRepository routineInfoRepository;

    @GetMapping("/test")
    String test() {
        return "test message";
    }

    @GetMapping("/routines")
    List<Routine> getRoutines() {
        return routineInfoRepository.getRoutines();
    }

    DefaultController(RoutineInfoRepository routineInfoRepository) {
        this.routineInfoRepository = routineInfoRepository;
    }
}
