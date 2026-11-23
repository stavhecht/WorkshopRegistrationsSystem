package patmal.workshops.student.impl.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Workshop;
import patmal.workshops.student.impl.logic.WorkshopSystemImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/** workshop-cli: fills a system with demo data and exports it to JSON in the current folder. */
public class Main {
    private static final Logger log = LogManager.getLogger(Main.class);
    private static final int MIN_STUDENTS = 3;

    /** Usage: java -jar workshop-cli.jar [students.csv] */
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            log.error("Usage: java -jar workshop-cli.jar [students.csv]");
            System.exit(1);
        }
        WorkshopSystem system = new WorkshopSystemImpl();

        Instructor dana = system.createInstructor("i1", "Dana Levi", "dana.levi@univ.edu");
        Workshop java = system.createWorkshop("Java Workshop", "Core Java and Maven");
        Workshop web = system.createWorkshop("Web Workshop", "HTTP, REST and JSON");
        system.associateWorkshopWithInstructor(dana, java);
        system.associateWorkshopWithInstructor(dana, web);

        List<String> ids = system.importStudentDataFromFile(args.length > 0 ? new File(args[0]) : bundledStudentsCsv())
                .stream().map(Student::getId).toList();

        // the demo needs several students: two teams and one free student
        if (ids.size() < MIN_STUDENTS) {
            log.error("The CSV has {} students, the demo needs at least {}. Nothing exported.", ids.size(), MIN_STUDENTS);
            System.exit(1);
        }

        // the last student stays free; the others are split between the two workshops
        List<String> inTeams = ids.subList(0, ids.size() - 1);
        int half = (inTeams.size() + 1) / 2;
        system.createTeam("Alpha", java, inTeams.subList(0, half));
        system.createTeam("Beta", web, inTeams.subList(half, inTeams.size()));

        File instructors = new File("instructors.json");
        File students = new File("students.json");
        File teams = new File("teams.json");
        system.exportInstructorsDataToJson(instructors);
        system.exportStudentsDataToJson(students);
        system.exportTeamsDataToJson(teams);

        log.info("Done. Files written to {}: {}, {}, {}",
                new File("").getAbsolutePath(), instructors, students, teams);
    }

    /** importStudentDataFromFile needs a File, and a resource inside the jar is not one, so copy it out. */
    private static File bundledStudentsCsv() throws IOException {
        Path tmp = Files.createTempFile("students", ".csv");
        tmp.toFile().deleteOnExit();
        try (InputStream in = Main.class.getResourceAsStream("/students.csv")) {
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
        }
        log.info("No CSV argument given, using the bundled students.csv");
        return tmp.toFile();
    }
}
