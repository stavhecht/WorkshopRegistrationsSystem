package patmal.workshops.student.impl.logic;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.*;
import patmal.workshops.student.impl.model.*;

import java.io.File;
import java.util.*;

/** In-memory registry. Unknown keys: getX(id) returns null, getStudentsOfX(id) returns an empty list. */
public class WorkshopSystemImpl implements WorkshopSystem {
    private static final Logger log = LogManager.getLogger(WorkshopSystemImpl.class);
    private final JsonExporter json = new JsonExporter();
    private final Map<String, InstructorImpl> instructors = new LinkedHashMap<>();
    private final Map<String, StudentImpl> students = new LinkedHashMap<>();
    private final Map<Integer, WorkshopImpl> workshops = new LinkedHashMap<>();
    private final Map<Integer, TeamImpl> teams = new LinkedHashMap<>();

    public WorkshopSystemImpl() { }

    @Override
    public Instructor createInstructor(String id, String name, String email) {
        if (instructors.containsKey(id)) throw reject("Duplicate instructor id: " + id);
        validateEmail(email);
        InstructorImpl i = new InstructorImpl(id, name, email);
        instructors.put(id, i);
        log.info("Created instructor {} ({})", id, name);
        return i;
    }

    @Override
    public Workshop createWorkshop(String name, String description) {
        WorkshopImpl w = new WorkshopImpl(workshops.size() + 1, name, description);
        workshops.put(w.getId(), w);
        log.info("Created workshop {} ({})", w.getId(), name);
        return w;
    }

    @Override
    public void associateWorkshopWithInstructor(Instructor instructor, Workshop workshop) {
        InstructorImpl i = instructors.get(instructor.getId());
        WorkshopImpl w = workshops.get(workshop.getId());
        if (i != instructor || w != workshop) throw reject("Unknown instructor or workshop");
        if (w.getInstructor() instanceof InstructorImpl old) old.removeWorkshop(w);
        w.setInstructor(i);
        i.addWorkshop(w);
        log.info("Workshop {} assigned to instructor {}", w.getId(), i.getId());
    }

    @Override
    public Student createStudent(String id, String name, String email) {
        if (students.containsKey(id)) throw reject("Duplicate student id: " + id);
        validateEmail(email);
        StudentImpl s = new StudentImpl(id, name, email);
        students.put(id, s);
        log.info("Created student {} ({})", id, name);
        return s;
    }

    @Override
    public Team createTeam(String name, Workshop workshop, List<String> members) {
        if (workshop == null || workshops.get(workshop.getId()) != workshop)
            throw reject("Unknown workshop");
        if (members.isEmpty()) throw reject("Team needs at least one member");
        // validate everything before touching state
        Set<String> seen = new HashSet<>();
        for (String id : members) {
            StudentImpl s = students.get(id);
            if (s == null) throw reject("Unknown student: " + id);
            if (s.getTeam() != null) throw reject("Student already in a team: " + id);
            if (!seen.add(id)) throw reject("Student listed twice: " + id);
        }
        TeamImpl t = new TeamImpl(teams.size() + 1, name, workshop);
        for (String id : members) t.addStudent(students.get(id));
        teams.put(t.getId(), t);
        log.info("Created team {} ({}) in workshop {} with {}", t.getId(), name, workshop.getId(), members);
        return t;
    }

    @Override public List<Team> getTeams() { return new ArrayList<>(teams.values()); }
    @Override public Team getTeam(Integer id) { return teams.get(id); }
    @Override public List<Student> getStudents() { return new ArrayList<>(students.values()); }
    @Override public Student getStudent(String id) { return students.get(id); }
    @Override public List<Workshop> getWorkshops() { return new ArrayList<>(workshops.values()); }
    @Override public Workshop getWorkshop(Integer id) { return workshops.get(id); }
    @Override public List<Instructor> getInstructors() { return new ArrayList<>(instructors.values()); }
    @Override public Instructor getInstructor(String id) { return instructors.get(id); }

    @Override
    public List<Student> getFreeStudents() {
        return students.values().stream().filter(s -> s.getTeam() == null).map(s -> (Student) s).toList();
    }

    @Override
    public List<Student> getStudentsOfInstructor(String instructorId) {
        InstructorImpl i = instructors.get(instructorId);
        List<Student> result = new ArrayList<>();
        if (i != null) for (Workshop w : i.getWorkshops()) result.addAll(getStudentsOfWorkshop(w.getId()));
        return result;
    }

    @Override
    public List<Student> getStudentsOfWorkshop(Integer workshopId) {
        List<Student> result = new ArrayList<>();
        for (TeamImpl t : teams.values())
            if (t.getWorkshop().getId().equals(workshopId)) result.addAll(t.getStudents());
        return result;
    }

    @Override
    public void exportInstructorsDataToJson(File target) {
        json.exportInstructors(getInstructors(), getTeams(), target);
        log.info("Exported {} instructors to {}", instructors.size(), target);
    }

    @Override
    public void exportStudentsDataToJson(File target) {
        json.exportStudents(students.values(), target);
        log.info("Exported {} students to {}", students.size(), target);
    }

    @Override
    public void exportTeamsDataToJson(File target) {
        json.exportTeams(getTeams(), target);
        log.info("Exported {} teams to {}", teams.size(), target);
    }

    private static void validateEmail(String email) {
        if (!EmailValidator.getInstance().isValid(email)) throw reject("Invalid e-mail address: " + email);
    }

    /** Logs a rejected operation and returns the exception for the caller to throw. */
    private static IllegalArgumentException reject(String message) {
        log.warn("Rejected: {}", message);
        return new IllegalArgumentException(message);
    }
}
