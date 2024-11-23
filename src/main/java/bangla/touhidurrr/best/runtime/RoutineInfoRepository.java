package bangla.touhidurrr.best.runtime;

import bangla.touhidurrr.best.models.Class;
import bangla.touhidurrr.best.models.Routine;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@Repository
public class RoutineInfoRepository {
    private final String dataSourceURL = "https://bubt-routine-data.pages.dev/routines.json";
    private HashMap<String, String> courseCodeToTitleMap;
    private HashMap<String, String> facultyIdToNameMap;
    private final List<Routine> routines = new ArrayList<>();
    private final HashMap<String, HashMap<Integer, List<String>>> programs = new HashMap<>();

    private void populateRoutines(JSONArray routinesArray) {
        for (int i = 0; i < routinesArray.length(); i++) {
            JSONObject routineObj = routinesArray.getJSONObject(i);

            int intake = routineObj.getInt("intake");
            String program = routineObj.getString("program");
            String section = routineObj.getString("section");
            String semester = routineObj.getString("semester");

            // Parse the classes 2D array
            JSONArray classesArray = routineObj.getJSONArray("classes");
            Class[][] classes = new Class[classesArray.length()][];

            for (int j = 0; j < classesArray.length(); j++) {
                JSONArray classRowArray = classesArray.getJSONArray(j);
                classes[j] = new Class[classRowArray.length()];

                for (int k = 0; k < classRowArray.length(); k++) {
                    if (classRowArray.isNull(k)) {
                        classes[j][k] = null; // Handle null class
                    } else {
                        JSONObject classObj = classRowArray.getJSONObject(k);
                        String courseCode = classObj.getString("courseCode");
                        String facultyCode = classObj.getString("facultyCode");
                        String building = classObj.getString("building");
                        String room = classObj.getString("room");

                        classes[j][k] = new Class(courseCode, facultyCode, building, room);
                    }
                }
            }

            // Construct Routine object
            Routine routine = new Routine(program, intake, section, semester, classes);
            routines.add(routine);
        }
    }

    private void populatePrograms(JSONObject programsObj) {
        for (Iterator<String> it = programsObj.keys(); it.hasNext(); ) {
            String programName = it.next();
            JSONObject curProgramObj = programsObj.getJSONObject(programName);
            HashMap<Integer, List<String>> program = new HashMap<>();

            for (Iterator<String> iter = curProgramObj.keys(); iter.hasNext(); ) {
                String intakeNumber = iter.next();
                JSONArray sectionsArr = curProgramObj.getJSONArray(intakeNumber);
                List<String> sections = new ArrayList<>();

                for (int i = 0; i < sectionsArr.length(); i++) {
                    sections.add(sectionsArr.getString(i));
                }

                program.put(Integer.parseInt(intakeNumber), sections);
            }

            programs.put(programName, program);
        }
    }

    public String getCourseName(String courseCode) {
        return courseCodeToTitleMap.get(courseCode);
    }

    public String getFacultyName(String facultyCode) {
        return facultyIdToNameMap.get(facultyCode);
    }

    public List<String> getProgramNames() {
        return new ArrayList<>(programs.keySet());
    }

    public List<Integer> getIntakes(String programName) {
        return new ArrayList<>(programs.get(programName).keySet());
    }

    public List<String> getSections(String programName, Integer intakeNumber) {
        return programs.get(programName).get(intakeNumber);
    }

    public List<Routine> getRoutines() {
        return routines;
    }

    @PostConstruct
    private void init() {
        try {
            // Fetch the JSON content from the URL
            InputStream inputStream = new URI(dataSourceURL).toURL().openStream();
            String jsonText = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            // Parse the JSON
            JSONObject jsonRoot = new JSONObject(jsonText);
            JSONObject programs = jsonRoot.getJSONObject("programs");
            JSONObject facultyIdToNameMapObject = jsonRoot.getJSONObject("facultyIdToNameMap");
            JSONObject courseCodeToTitleMapObject = jsonRoot.getJSONObject("courseCodeToTitleMap");
            JSONArray routines = jsonRoot.getJSONArray("routines");

            this.facultyIdToNameMap = new ObjectMapper().readValue(facultyIdToNameMapObject.toString(), HashMap.class);
            this.courseCodeToTitleMap = new ObjectMapper().readValue(courseCodeToTitleMapObject.toString(), HashMap.class);

            populateRoutines(routines);
            populatePrograms(programs);

            System.out.println(getProgramNames());
            System.out.println(getIntakes("B.Sc. Engg. in CSE"));
            System.out.println(getSections("B.Sc. Engg. in CSE", 50));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
