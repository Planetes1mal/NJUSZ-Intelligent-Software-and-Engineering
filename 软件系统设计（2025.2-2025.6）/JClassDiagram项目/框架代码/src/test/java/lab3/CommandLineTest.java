package lab3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import command.CommandLineTool;
import diagram.ClassDiagram;
import diagram.ClassDiagramGenerator;

public class CommandLineTest {
    @Test
    public void testAddCommand() throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource("Student.java");
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        CommandLineTool commandLine = new CommandLineTool(diagram);

        commandLine.execute("add -c Teacher");
        commandLine.execute("add -i Member");

        String uml = diagram.generateUML();

        assertTrue(uml.contains("class Teacher {\n}"), "Teacher class should be added");
        assertTrue(uml.contains("interface Member {\n}"), "Member interface should be added");

        String expected_student_class = """
                class Student {
                    - courses: List<Course>
                    # id: int
                    + name: String
                    + addCourse(course: Course): void
                }
                """;
        assertTrue(uml.contains(expected_student_class),
                "You should output the correct UML for the Student class:" + expected_student_class);
        assertTrue(uml.contains("Course <-- Student"), "Missing Course <-- Student");
    }

    @Test
    public void testAddEnumCommand() throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource("Student.java");
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        CommandLineTool commandLine = new CommandLineTool(diagram);

        commandLine.execute("add -e RoleType --values=TEACHER,STUDENT");
        commandLine.execute("add -c Member --abstract");
        String uml = diagram.generateUML();

        assertTrue(uml.contains("enum RoleType {\n    TEACHER\n    STUDENT\n}")
                || uml.contains("enum RoleType {\n    STUDENT\n    TEACHER\n}"), "RoleType enum should be added");
        assertTrue(uml.contains("abstract class Member {\n}"), "Member abstract class should be added");

        String expected_student_class = """
                class Student {
                    - courses: List<Course>
                    # id: int
                    + name: String
                    + addCourse(course: Course): void
                }
                """;

        assertTrue(uml.contains(expected_student_class),
                "You should output the correct UML for the Student class:\n" + expected_student_class);
        assertTrue(uml.contains("Course <-- Student"), "Missing Course <-- Student");
    }

    @Test
    public void testDeleteCommand() throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource("Student.java");
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        CommandLineTool commandLine = new CommandLineTool(diagram);

        commandLine.execute("delete -c Course");

        String uml = diagram.generateUML();
        String expected_student_class = """
                class Student {
                    - courses: List<Course>
                    # id: int
                    + name: String
                    + addCourse(course: Course): void
                }
                """;

        assertTrue(uml.contains(expected_student_class),
                "You should output the correct UML for the Student class:" + expected_student_class);
        assertTrue(!uml.contains("class Course {"), "Course should be deleted");
        assertTrue(!uml.contains("Course <-- Student"), "Course <-- Student should be deleted");
    }

    @Test
    public void testAddDeleteCommand() throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource("Student.java");
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        CommandLineTool commandLine = new CommandLineTool(diagram);

        commandLine.execute("delete -c Course");
        commandLine.execute("add -e RoleType --values=TEACHER,STUDENT");
        commandLine.execute("add -c Member --abstract");

        String uml = diagram.generateUML();
        
        assertTrue(uml.contains("enum RoleType {\n    TEACHER\n    STUDENT\n}")
                || uml.contains("enum RoleType {\n    STUDENT\n    TEACHER\n}"), "RoleType enum should be added");
        assertTrue(uml.contains("abstract class Member {\n}"), "Member abstract class should be added");

        commandLine.execute("delete -e RoleType");

        uml = diagram.generateUML();
        String expected_student_class = """
                class Student {
                    - courses: List<Course>
                    # id: int
                    + name: String
                    + addCourse(course: Course): void
                }
                """;

        assertTrue(uml.contains(expected_student_class),
                "You should output the correct UML for the Student class:" + expected_student_class);
        assertTrue(!uml.contains("class Course {"), "Course should be deleted");
        assertTrue(!uml.contains("Course <-- Student"), "Course <-- Student should be deleted");
        assertTrue(!uml.contains("enum RoleType {\n    TEACHER\n    STUDENT\n}")
                && !uml.contains("enum RoleType {\n    STUDENT\n    TEACHER\n}"), "RoleType enum should be deleted");
    }

    @Test
    public void testAddDeleteUndoCommand() throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource("Student.java");
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        CommandLineTool commandLine = new CommandLineTool(diagram);

        commandLine.execute("delete -c Course");
        String uml = diagram.generateUML();

        assertTrue(!uml.contains("class Course {"), "Course should be deleted");
        assertTrue(!uml.contains("Course <-- Student"), "Course <-- Student should be deleted");       
        
        commandLine.execute("undo");
        uml = diagram.generateUML();
        assertTrue(uml.contains("class Course {"), "after undo: Course should exist");
        assertTrue(uml.contains("Course <-- Student"), "after undo: Course <-- Student should exist");

        String result = commandLine.execute("undo");
        assertTrue(result.equals("No command to undo"), "You should report No command to undo");

        commandLine.execute("add -c Tree");
        commandLine.execute("add -c Road");
        commandLine.execute("add -c Stone");

        uml = diagram.generateUML();
        assertTrue(uml.contains("class Tree {"), "Tree should be added");
        assertTrue(uml.contains("class Road {"), "Road should be added");
        assertTrue(uml.contains("class Stone {"), "Stone should be added");

        commandLine.execute("undo");
        commandLine.execute("undo");
        uml = diagram.generateUML();

        assertTrue(uml.contains("class Tree {"), "after undo:Tree should exist");
        assertTrue(!uml.contains("class Road {"), "after undo: Road should not exist");
        assertTrue(!uml.contains("class Stone {"), "after undo: Stone should not exist");
        
    }

    @Test
    public void testAddFieldCommand() throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource("Student.java");
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        CommandLineTool commandLine = new CommandLineTool(diagram);

        commandLine.execute("add field Course -n studentNumber -t int");

        String uml = diagram.generateUML();
        String expected_student_class = """
                class Course {
                    - studentNumber: int
                    # credits: int
                    + name: String
                    + updateCredits(credits: int): void
                }
                """;
        assertTrue(uml.contains(expected_student_class),
                "You should output the correct UML for the Course class:\n" + expected_student_class);
        assertTrue(uml.contains("Course <-- Student"), "Missing Course <-- Student");
        
        commandLine.execute("add field Course -n courseNumber -t int --access=+ --static");

        uml = diagram.generateUML();

        String expected_student_class_field = "+ {static} courseNumber: int";
        assertTrue(uml.contains(expected_student_class_field),
                "You should output the correct UML for the Course class:\n" + expected_student_class_field);
    }

    @Test
    public void testDeleteFieldCommand() throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource("Student.java");
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        CommandLineTool commandLine = new CommandLineTool(diagram);

        commandLine.execute("delete field Student -n name");
        commandLine.execute("delete field Student -n id");

        String uml = diagram.generateUML();
        String expected_student_class = """
                class Student {
                    - courses: List<Course>
                    + addCourse(course: Course): void
                }
                """;
        assertTrue(uml.contains(expected_student_class),
                "You should output the correct UML for the Student class:" + expected_student_class);

        assertTrue(!uml.contains("# id: int"), "id field should be deleted");

        commandLine.execute("delete field Course -n credits");

        uml = diagram.generateUML();
        assertTrue(!uml.contains("+ credits: int"), "credits field should be deleted");
    }

    @Test
    public void testModifyFieldCommand() throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource("Student.java");
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        CommandLineTool commandLine = new CommandLineTool(diagram);

        commandLine.execute("modify field Course -n credits --new-access=+ --static");
        commandLine.execute("modify field Student -n id --new-type=String");
        commandLine.execute("modify field Student -n courses --new-type=List<String>");
        
        String uml = diagram.generateUML();
        assertTrue(uml.contains("+ {static} credits: int"), "credits field should be modified");
        assertTrue(uml.contains("# id: String"), "id field should be modified");
        assertTrue(uml.contains("- courses: List<String>"), "courses field should be modified");
        assertTrue(!uml.contains("- courses: List<Course>"), "courses field should be modified");
    } 

    @Test
    public void testUndoModifyFieldCommand() throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource("Student.java");
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        CommandLineTool commandLine = new CommandLineTool(diagram);

        commandLine.execute("modify field Course -n credits --new-access=+ --static");
        commandLine.execute("modify field Student -n id --new-type=String");
        commandLine.execute("modify field Student -n courses --new-type=List<String>");
        
        String uml = diagram.generateUML();
        assertTrue(uml.contains("+ {static} credits: int"), "credits field should be modified");
        assertTrue(uml.contains("# id: String"), "id field should be modified");
        assertTrue(uml.contains("- courses: List<String>"), "courses field should be modified");
        assertTrue(!uml.contains("- courses: List<Course>"), "courses field should be modified");

        commandLine.execute("undo");
        commandLine.execute("undo");
        uml = diagram.generateUML();
        assertTrue(uml.contains("+ {static} credits: int"), "credits field should be modified");
        assertTrue(uml.contains("# id: int"), "after undo: id field should not be modified");
        assertTrue(uml.contains("- courses: List<Course>"), "after undo:courses field should not be modified");
    }

    @Test
    public void testAddMethodCommand() throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource("Student.java");
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        CommandLineTool commandLine = new CommandLineTool(diagram);

        commandLine.execute("add function Course -n getStudentNumber -t int");
        String uml = diagram.generateUML();
        assertTrue(uml.contains("- getStudentNumber(): int"), "getStudentNumber method should be added");

        commandLine.execute("add function Course -n getAllStudentNumber -t Integer --access=+ --static");
        uml = diagram.generateUML();
        assertTrue(uml.contains("+ {static} getAllStudentNumber(): Integer"), "getAllStudentNumber method should be added");

        commandLine.execute("add function Student -n modifyInformation -t void --params=name:String,id:int --access=+");
        uml = diagram.generateUML();
        assertTrue(uml.contains("+ modifyInformation(name: String, id: int): void"), "modifyInformation method should be added");
    }

    @Test
    public void testDeleteMethodCommand() throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource("Student.java");
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        CommandLineTool commandLine = new CommandLineTool(diagram);

        commandLine.execute("delete function Course -n updateCredits");
        commandLine.execute("delete function Student -n addCourse");

        String uml = diagram.generateUML();
        
        String expected_course_class = """
            class Course {
                # credits: int
                + name: String
            }
            """;
        assertTrue(uml.contains(expected_course_class), "Course class should exist:\n" + expected_course_class);

        String expected_student_class = """
            class Student {
                - courses: List<Course>
                # id: int
                + name: String
            }
            """;
        assertTrue(uml.contains(expected_student_class), "Student class should exist:\n" + expected_student_class);

        assertTrue(!uml.contains("+ updateCredits(credits: int): void"), "updateCredits method should be deleted");
        assertTrue(!uml.contains("+ addCourse(course: Course): void"), "addCourse method should be deleted");
    }

    @Test
    public void testModifyMethodCommand() throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource("Student.java");
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        CommandLineTool commandLine = new CommandLineTool(diagram);

        commandLine.execute("modify function Course -n updateCredits --new-name=updateCredit --new-access=# --abstract");
        commandLine.execute("modify function Student -n addCourse --new-params=course:Course,teacher:String");

        String uml = diagram.generateUML();
        assertTrue(uml.contains("# {abstract} updateCredit(credits: int): void"), "updateCredits method should be modified");
        
        assertTrue(uml.contains("+ addCourse(course: Course, teacher: String): void"), "addCourse method should be modified");
        
        commandLine.execute("modify function Course -n updateCredit --new-return=boolean");
        uml = diagram.generateUML();
        assertTrue(uml.contains("# updateCredit(credits: int): boolean"), "updateCredits method should be modified");
    }

    @Test
    public void testUndoModifyMethodCommand() throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource("Student.java");
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        CommandLineTool commandLine = new CommandLineTool(diagram);

        commandLine.execute("modify function Course -n updateCredits --new-return=boolean");
        commandLine.execute("modify function Student -n addCourse --new-params=course:Course,teacher:String");

        String uml = diagram.generateUML();

        assertTrue(uml.contains("+ updateCredits(credits: int): boolean"), "updateCredits method should be modified");
        assertTrue(uml.contains("+ addCourse(course: Course, teacher: String): void"), "addCourse method should be modified");
        
        commandLine.execute("undo");
        commandLine.execute("undo");
        uml = diagram.generateUML();
        assertTrue(uml.contains("+ updateCredits(credits: int): void"), "after undo: updateCredits method should not be modified");
        assertTrue(uml.contains("+ addCourse(course: Course): void"), "after undo: addCourse method should not be modified");
    }

    @Test
    public void testQueryCommand() throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource("Student.java");
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        CommandLineTool commandLine = new CommandLineTool(diagram);

        String result = commandLine.execute("query -c Student");
        String expected_student_class = """
            class Student {
                - courses: List<Course>
                # id: int
                + name: String
                + addCourse(course: Course): void
            }
            """;
        assertEquals(expected_student_class, result, "You should output the correct UML for the Student class");
    
        result = commandLine.execute("query -c Student --hide=field");
        String expected_student_class_hide_field = """
            class Student {
                + addCourse(course: Course): void
            }
            """;
        assertEquals(expected_student_class_hide_field, result, "You should output the correct UML for the Student class");
        
        result = commandLine.execute("query -c Student --hide=method");
        String expected_student_class_hide_method = """
            class Student {
                - courses: List<Course>
                # id: int
                + name: String
            }
            """;
        assertEquals(expected_student_class_hide_method, result, "You should output the correct UML for the Student class");

        resourceUrl = getClass().getResource("Duck.java");
        resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        diagram = generator.parse(resourcePath);
        commandLine = new CommandLineTool(diagram);

        result = commandLine.execute("query -i QuackBehavior");
        String expected_quack_behavior_interface = """
            interface QuackBehavior {
                + quack(): void
            }
            """;
        assertEquals(expected_quack_behavior_interface, result, "You should output the correct UML for the QuackBehavior interface");
        
    }

    @Test
    public void testRelateCommand() throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource("Duck.java");
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        CommandLineTool commandLine = new CommandLineTool(diagram);
        
        String result = commandLine.execute("relate Duck QuackBehavior");
        assertEquals("QuackBehavior <-- Duck\n", result);
        result = commandLine.execute("relate Farmer Duck");
        assertEquals("Duck <.. Farmer\n", result);
        result = commandLine.execute("relate RedDuck Duck");
        assertEquals("Duck <|-- RedDuck\n", result);
        result = commandLine.execute("relate Farmer RedDuck");
        assertTrue(result.equals("Farmer <-- RedDuck\nRedDuck <.. Farmer\n") || result.equals("RedDuck <.. Farmer\nFarmer <-- RedDuck\n"), "relate Farmer RedDuck error");
        result = commandLine.execute("relate QuackBehavior MuteQuack");
        assertEquals("QuackBehavior <|.. MuteQuack\n", result);
    }

    @Test
    public void testSmellCommand() throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource("Duck.java");
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        CommandLineTool commandLine = new CommandLineTool(diagram);

        String result = commandLine.execute("smell detail RedDuck");

        String expected_result_1 = """
            Lazy Class: RedDuck
            Circular Dependency: RedDuck <.. Farmer <.. RedDuck
            """;
        String expected_result_2 = """
            Circular Dependency: RedDuck <.. Farmer <.. RedDuck
            Lazy Class: RedDuck
            """;
        String expected_result_3 = """
            Lazy Class: RedDuck
            Circular Dependency: Farmer <.. RedDuck <.. Farmer
            """;
        String expected_result_4 = """
            Circular Dependency: Farmer <.. RedDuck <.. Farmer
            Lazy Class: RedDuck
            """;

        assertTrue(expected_result_1.equals(result) || expected_result_2.equals(result) || expected_result_3.equals(result) || expected_result_4.equals(result), "You should output the correct result like:\n" + expected_result_1);
        
        result = commandLine.execute("smell detail FlyWithWings");
        assertEquals("Lazy Class: FlyWithWings\n", result);
    }

    @Test
    public void testDeepUndoCommand() throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource("Student.java");
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        CommandLineTool commandLine = new CommandLineTool(diagram);

        for (int i = 0; i < 10; i++) {
            commandLine.execute("add -c TreeClass" + String.valueOf(i));
            String result = commandLine.execute("query -c TreeClass" + String.valueOf(i));
            assertTrue(result.contains("class TreeClass" + String.valueOf(i)), "TreeClass" + String.valueOf(i) + " should exist");
        }

        for (int i = 0; i < 5; i++) {
            commandLine.execute("undo");
        }

        String uml = diagram.generateUML();
        for (int i = 0; i < 5; i++) {
            assertTrue(uml.contains("class TreeClass" + String.valueOf(i)), "TreeClass" + String.valueOf(i) + " should exist");
            assertTrue(!uml.contains("class TreeClass" + String.valueOf(i + 5)), "TreeClass" + String.valueOf(i + 5) + " should not exist");
        }
        for (int i = 0; i < 5; i++) {
            commandLine.execute("undo");
        }
        String result = commandLine.execute("undo");
        assertTrue(result.equals("No command to undo"), "You should report No command to undo");
    }
}