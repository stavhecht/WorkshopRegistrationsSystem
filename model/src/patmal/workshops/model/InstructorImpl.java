package patmal.workshops.model;

import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Workshop;

import java.util.ArrayList;
import java.util.List;

public class InstructorImpl extends PersonImpl implements Instructor {
    private final List<Workshop> workshops = new ArrayList<>();

    public InstructorImpl(String id, String name, String email) { super(id, name, email); }

    @Override public List<Workshop> getWorkshops() { return new ArrayList<>(workshops); }

    public void addWorkshop(Workshop w) { if (!workshops.contains(w)) workshops.add(w); }
    public void removeWorkshop(Workshop w) { workshops.remove(w); }
}
