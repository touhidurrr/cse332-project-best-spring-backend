package bangla.touhidurrr.best.runtime;

import bangla.touhidurrr.best.models.Class;
import bangla.touhidurrr.best.models.*;
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
    private List<Routine> routines;
    private HashMap<String, HashMap<Integer, List<String>>> programs;
    private HashMap<String, String> courseCodeToTitleMap;
    private HashMap<String, String> facultyIdToNameMap;
    private HashMap<String, HashMap<String, List<RoutineClass>>> buildingRoomRoutineClassesMap;

    private void populateRoutines(JSONArray routinesArray) {
        routines = new ArrayList<>();
        buildingRoomRoutineClassesMap = new HashMap<>();

        for (int i = 0; i < routinesArray.length(); i++) {
            JSONObject routineObj = routinesArray.getJSONObject(i);

            int intake = routineObj.getInt("intake");
            String program = routineObj.getString("program");
            String section = routineObj.getString("section");
            String semester = routineObj.getString("semester");

            // parse periods
            JSONArray periodsArr = routineObj.getJSONArray("periods");
            String[] periods = new String[periodsArr.length()];
            for (int pi = 0; pi < periodsArr.length(); pi++) {
                periods[pi] = periodsArr.getString(pi);
            }

            // Parse the classes 2D array
            JSONArray classesArray = routineObj.getJSONArray("classes");
            Class[][] classes = new Class[classesArray.length()][];

            for (int dIdx = 0; dIdx < classesArray.length(); dIdx++) {
                JSONArray classRowArray = classesArray.getJSONArray(dIdx);
                classes[dIdx] = new Class[classRowArray.length()];

                for (int pIdx = 0; pIdx < classRowArray.length(); pIdx++) {
                    if (classRowArray.isNull(pIdx)) {
                        classes[dIdx][pIdx] = null; // Handle null class
                    } else {
                        JSONObject classObj = classRowArray.getJSONObject(pIdx);
                        String courseCode = classObj.getString("courseCode");
                        String facultyCode = classObj.getString("facultyCode");
                        String building = classObj.getString("building");
                        String room = classObj.getString("room");

                        classes[dIdx][pIdx] = new Class(
                                getCourse(courseCode),
                                getFaculty(facultyCode),
                                building, room
                        );

                        // add to buildingRoomClassesMap
                        if (!buildingRoomRoutineClassesMap.containsKey(building)) {
                            buildingRoomRoutineClassesMap.put(building, new HashMap<>());
                        } else {
                            if (!buildingRoomRoutineClassesMap.get(building).containsKey(room)) {
                                buildingRoomRoutineClassesMap.get(building).put(room, new ArrayList<>());
                            } else {
                                buildingRoomRoutineClassesMap.get(building).get(room).add(
                                        new RoutineClass(dIdx, pIdx, classes[dIdx][pIdx])
                                );
                            }
                        }
                    }
                }
            }

            routines.add(new Routine(program, intake, section, semester, periods, classes));
        }
    }

    private void populatePrograms(JSONObject programsObj) {
        programs = new HashMap<>();

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

    public List<String> getBuildings() {
        return new ArrayList<>(buildingRoomRoutineClassesMap.keySet());
    }

    public List<String> getRooms(String building) {
        return new ArrayList<>(buildingRoomRoutineClassesMap.get(building).keySet());
    }

    public HashMap<String, List<String>> getBuildingRoomsMap() {
        HashMap<String, List<String>> buildingRoomsMap = new HashMap<>();

        for (String building : buildingRoomRoutineClassesMap.keySet()) {
            buildingRoomsMap.put(
                    building,
                    new ArrayList<>(buildingRoomRoutineClassesMap.get(building).keySet())
            );
        }

        return buildingRoomsMap;
    }

    public List<RoutineClass> getClasses() {
        List<RoutineClass> rClasses = new ArrayList<>();

        for (HashMap<String, List<RoutineClass>> roomClassesMap : buildingRoomRoutineClassesMap.values()) {
            for (List<RoutineClass> roomClasses : roomClassesMap.values()) {
                rClasses.addAll(roomClasses);
            }
        }

        return rClasses;
    }

    public List<RoutineClass> getClassesInABuilding(String building) {
        List<RoutineClass> classes = new ArrayList<>();

        for (List<RoutineClass> classesInRooms : buildingRoomRoutineClassesMap.get(building).values()) {
            classes.addAll(classesInRooms);
        }

        return classes;
    }

    public List<RoutineClass> getClassesInABuildingAndRoom(String building, String room) {
        return buildingRoomRoutineClassesMap.get(building).get(room);
    }

    public String getCourseName(String courseCode) {
        return courseCodeToTitleMap.get(courseCode);
    }

    public List<String> getCourseCodes() {
        return new ArrayList<>(courseCodeToTitleMap.keySet());
    }

    public Course getCourse(String courseCode) {
        return new Course(courseCode, getCourseName(courseCode));
    }

    public Faculty getFaculty(String facultyCode) {
        return new Faculty(facultyCode, getFacultyName(facultyCode));
    }

    public CourseInfo getCourseInfo(String courseCode) {
        HashMap<String, Integer> facultyCodeToCount = new HashMap<>();
        routines.forEach(routine -> {
            Class[][] classes = routine.classes();
            for (Class[] aClass : classes) {
                for (Class cls : aClass) {
                    if (cls != null && cls.course().code().equals(courseCode)) {
                        facultyCodeToCount.merge(cls.faculty().code(), 1, Integer::sum);
                    }
                }
            }
        });

        return new CourseInfo(
                getCourse(courseCode),
                facultyCodeToCount.entrySet().stream().map(
                        entry -> new CourseFaculty(getFaculty(entry.getKey()), entry.getValue())
                ).toList()
        );
    }

    public String getFacultyName(String facultyCode) {
        return facultyIdToNameMap.get(facultyCode);
    }

    public List<String> getFacultyCodes() {
        return new ArrayList<>(facultyIdToNameMap.keySet());
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


    public List<FacultyClass> getFacultyClasses(String facultyCode) {
        List<FacultyClass> facultyClasses = new ArrayList<>();
        routines.forEach(routine -> {
            Class[][] classes = routine.classes();
            String[] periods = routine.periods();
            for (int day = 0; day < classes.length; day++) {
                for (int period = 0; period < classes[day].length; period++) {
                    Class cls = classes[day][period];
                    if (cls == null) continue;

                    if (cls.faculty().code().equals(facultyCode)) {
                        facultyClasses.add(
                                new FacultyClass(
                                        cls.course(), cls.building(), cls.room(),
                                        day, period, periods[period],
                                        routine.program(), routine.intake(), routine.section()
                                )
                        );
                    }
                }
            }
        });
        return facultyClasses;
    }

    private void reloadData() {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    private void init() {
        reloadData();
    }
}
