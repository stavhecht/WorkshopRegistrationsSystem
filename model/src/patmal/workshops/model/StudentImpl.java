package patmal.workshops.model;

import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;

import java.util.ArrayList;
import java.util.List;

public class StudentImpl extends PersonImpl implements Student {
    private Team team;

    public StudentImpl(String id, String name, String email) { super(id, name, email); }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    @Override public String getTeamName() { return team == null ? null : team.getName(); }

    @Override public Instructor getInstructor() { return team == null ? null : team.getWorkshop().getInstructor(); }

    @Override public List<Student> getTeamMates() {
        List<Student> mates = new ArrayList<>();
        if (team != null) {
            for (Student s : team.getStudents()) if (s != this) mates.add(s);
        }
        return mates;
    }
}
