package patmal.workshops.student.impl.logic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import patmal.workshops.api.model.*;
import patmal.workshops.student.impl.model.StudentImpl;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Builds the export trees as plain maps/lists and lets Gson write them.
 * Each tree only drills down, so a child never points back at an ancestor (no cycles).
 */
class JsonExporter {
    private final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

    void exportInstructors(List<Instructor> instructors, List<Team> allTeams, File target) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Instructor i : instructors) {
            List<Map<String, Object>> workshops = new ArrayList<>();
            for (Workshop w : i.getWorkshops()) {
                Map<String, Object> wm = workshop(w);
                List<Map<String, Object>> teams = new ArrayList<>();
                for (Team t : allTeams) {
                    if (!t.getWorkshop().getId().equals(w.getId())) continue;
                    Map<String, Object> tm = team(t);
                    tm.put("students", persons(t.getStudents()));
                    teams.add(tm);
                }
                wm.put("teams", teams);
                workshops.add(wm);
            }
            Map<String, Object> im = person(i);
            im.put("workshops", workshops);
            out.add(im);
        }
        write(out, target);
    }

    void exportStudents(Collection<StudentImpl> students, File target) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (StudentImpl s : students) {
            Team t = s.getTeam();
            Map<String, Object> tm = null;
            if (t != null) {
                tm = team(t);
                tm.put("studentIds", t.getStudents().stream().map(Student::getId).toList());
                tm.put("workshop", workshopWithInstructor(t.getWorkshop()));
            }
            Map<String, Object> sm = person(s);
            sm.put("team", tm);
            out.add(sm);
        }
        write(out, target);
    }

    void exportTeams(List<Team> teams, File target) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Team t : teams) {
            Map<String, Object> tm = team(t);
            tm.put("students", persons(t.getStudents()));
            tm.put("workshop", workshopWithInstructor(t.getWorkshop()));
            out.add(tm);
        }
        write(out, target);
    }

    private static Map<String, Object> person(Person p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("name", p.getName());
        m.put("email", p.getEmail());
        return m;
    }

    private static List<Map<String, Object>> persons(List<? extends Person> ps) {
        return ps.stream().map(JsonExporter::person).toList();
    }

    private static Map<String, Object> workshop(Workshop w) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", w.getId());
        m.put("name", w.getName());
        m.put("description", w.getDescription());
        return m;
    }

    private static Map<String, Object> workshopWithInstructor(Workshop w) {
        Map<String, Object> m = workshop(w);
        m.put("instructor", w.getInstructor() == null ? null : person(w.getInstructor()));
        return m;
    }

    private static Map<String, Object> team(Team t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("name", t.getName());
        return m;
    }

    private void write(Object data, File target) {
        try {
            Files.writeString(target.toPath(), gson.toJson(data), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write " + target, e);
        }
    }
}
