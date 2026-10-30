package patmal.workshops.logic;

import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.*;
import patmal.workshops.model.*;

import java.util.*;

/** In-memory registry. Unknown keys: getX(id) returns null, getStudentsOfX(id) returns an empty list. */
public class WorkshopSystemImpl implements WorkshopSystem {
    private final Map<String, InstructorImpl> instructors = new LinkedHashMap<>();
    private final Map<String, StudentImpl> students = new LinkedHashMap<>();
    private final Map<Integer, WorkshopImpl> workshops = new LinkedHashMap<>();
    private final Map<Integer, TeamImpl> teams = new LinkedHashMap<>();

    public WorkshopSystemImpl() { }

    @Override
    public Instructor createInstructor(String id, String name, String email) {
        if (instructors.containsKey(id)) throw new IllegalArgumentException("Duplicate instructor id: " + id);
        InstructorImpl i = new InstructorImpl(id, name, email);
        instructors.put(id, i);
        return i;
    }

    @Override
    public Workshop createWorkshop(String name, String description) {
        WorkshopImpl w = new WorkshopImpl(workshops.size() + 1, name, description);
        workshops.put(w.getId(), w);
        return w;
    }

    @Override
    public void associateWorkshopWithInstructor(Instructor instructor, Workshop workshop) {
        InstructorImpl i = instructors.get(instructor.getId());
        WorkshopImpl w = workshops.get(workshop.getId());
        if (i != instructor || w != workshop) throw new IllegalArgumentException("Unknown instructor or workshop");
        if (w.getInstructor() instanceof InstructorImpl old) old.removeWorkshop(w);
        w.setInstructor(i);
        i.addWorkshop(w);
    }

    @Override
    public Student createStudent(String id, String name, String email) {
        if (students.containsKey(id)) throw new IllegalArgumentException("Duplicate student id: " + id);
        StudentImpl s = new StudentImpl(id, name, email);
        students.put(id, s);
        return s;
    }

    @Override
    public Team createTeam(String name, Workshop workshop, List<String> members) {
        if (workshop == null || workshops.get(workshop.getId()) != workshop)
            throw new IllegalArgumentException("Unknown workshop");
        if (members.isEmpty()) throw new IllegalArgumentException("Team needs at least one member");
        // validate everything before touching state
        Set<String> seen = new HashSet<>();
        for (String id : members) {
            StudentImpl s = students.get(id);
            if (s == null) throw new IllegalArgumentException("Unknown student: " + id);
            if (s.getTeam() != null) throw new IllegalArgumentException("Student already in a team: " + id);
            if (!seen.add(id)) throw new IllegalArgumentException("Student listed twice: " + id);
        }
        TeamImpl t = new TeamImpl(teams.size() + 1, name, workshop);
        for (String id : members) t.addStudent(students.get(id));
        teams.put(t.getId(), t);
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
}
