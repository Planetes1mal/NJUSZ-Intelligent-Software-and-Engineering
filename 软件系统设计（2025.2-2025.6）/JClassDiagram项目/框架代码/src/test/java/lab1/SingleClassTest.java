package lab1;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import diagram.ClassDiagram;
import diagram.ClassDiagramGenerator;

public class SingleClassTest {

    public String generateUML(String testcase) throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource(testcase);
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI()); 
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        return diagram.generateUML();
    }
    
    @Test
    public void testEmpty() throws URISyntaxException, IOException {
        String uml = generateUML("Empty.java"); 
        assertTrue(uml.startsWith("@startuml"), "UML diagram should start with @startuml");
        String expected = "class Empty {\n" +
                          "}";
        assertTrue(uml.contains(expected), "UML diagram should contain class Empty");
        assertTrue(uml.contains("@enduml"));
    }
    @Test
    public void testField() throws URISyntaxException, IOException {
        String uml = generateUML("Student.java");
        assertTrue(uml.startsWith("@startuml"));
        String expected = "class Student {\n" +
                          "    - name: String\n" +
                          "    + age: int\n" +
                          "}";
        assertTrue(uml.contains(expected));
        assertTrue(uml.contains("@enduml"));
    }

    @Test
    public void testMultiField() throws URISyntaxException, IOException {
        String uml = generateUML("MultiField.java");
        assertTrue(uml.startsWith("@startuml"));
        String expected = "class MultiField {\n" +
                          "    - c: String\n" +
                          "    # b: double\n" +
                          "    ~ d: int\n" +
                          "    + {static} a: int\n" +
                          "}";
        assertTrue(uml.contains(expected));
        assertTrue(uml.contains("@enduml"));
    }

    @Test
    public void testMethod() throws URISyntaxException, IOException {
        String uml = generateUML("Method.java");
        assertTrue(uml.startsWith("@startuml"));
        String expected = "class Method {\n" +
                          "    - check(): void\n" +
                          "    # minus(a: double): double\n";
        assertTrue(uml.contains(expected));
        assertTrue(uml.contains("+ calculate(a: int, b: int): int"));
        assertTrue(uml.contains("+ calculate(a: int, b: int, c: String): String"));
        assertTrue(uml.contains("@enduml"));
    }

    @Test
    public void testSingleClass() throws URISyntaxException, IOException {
        String uml = generateUML("SingleClass.java");
        assertTrue(uml.startsWith("@startuml"));
        String expected = "class SingleClass {\n" +
                          "    - b: String\n" +
                          "    + a: int\n" +
                          "    - {static} times10(x: int): int\n" +
                          "    # setB(b: String): void\n" +
                          "    + getA(): int\n" +
                          "}";
        assertTrue(uml.contains(expected));
        assertTrue(uml.contains("@enduml"));
    }
}
