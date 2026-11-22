package patmal.workshops.student.impl.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Workshop;
import patmal.workshops.student.impl.logic.WorkshopSystemImpl;

import java.io.File;
import java.util.List;

/** workshop-cli: fills a system with demo data and exports it to JSON in the current folder. */
public class Main {
    private static final Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        WorkshopSystem system = new WorkshopSystemImpl();

        Instructor dana = system.createInstructor("i1", "Dana Levi", "dana.levi@univ.edu");
        Workshop java = system.createWorkshop("Java Workshop", "Core Java and Maven");
        Workshop web = system.createWorkshop("Web Workshop", "HTTP, REST and JSON");
        system.associateWorkshopWithInstructor(dana, java);
        system.associateWorkshopWithInstructor(dana, web);

        system.createStudent("s1", "Alice Cohen", "alice@students.edu");
        system.createStudent("s2", "Bob Friedman", "bob@students.edu");
        system.createStudent("s3", "Carmel Mizrahi", "carmel@students.edu");
        system.createStudent("s4", "David Peretz", "david@students.edu"); // stays free

        system.createTeam("Alpha", java, List.of("s1", "s2"));
        system.createTeam("Beta", web, List.of("s3"));

        File instructors = new File("instructors.json");
        File students = new File("students.json");
        File teams = new File("teams.json");
        system.exportInstructorsDataToJson(instructors);
        system.exportStudentsDataToJson(students);
        system.exportTeamsDataToJson(teams);

        log.info("Done. Files written to {}: {}, {}, {}",
                new File("").getAbsolutePath(), instructors, students, teams);
    }
}
