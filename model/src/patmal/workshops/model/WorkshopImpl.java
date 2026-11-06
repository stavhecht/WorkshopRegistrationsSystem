package patmal.workshops.model;

import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Workshop;

import java.util.Objects;

public class WorkshopImpl implements Workshop {
    private final Integer id;
    private final String name, description;
    private Instructor instructor;

    public WorkshopImpl(Integer id, String name, String description){
        this.id = id;
        this.name = name;
        this.description = description;
    }

    @Override public Integer getId() { return id; }
    @Override public String getName() { return name; }
    @Override public String getDescription() { return description; }
    @Override public Instructor getInstructor() { return instructor; }

    public void setInstructor(Instructor instructor) { this.instructor = instructor; }

    // shortcut: identity is the id only; fine because WorkshopSystemImpl assigns unique ids
    @Override public boolean equals(Object o) { return o instanceof WorkshopImpl w && Objects.equals(id, w.id); }
    @Override public int hashCode() { return Objects.hashCode(id); }
}
