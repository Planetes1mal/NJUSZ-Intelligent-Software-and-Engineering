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

public class MultiClassTest {

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
    public void testMultiEmpty() throws URISyntaxException, IOException {
        String uml = generateUML("MultiEmpty.java"); 
        assertTrue(uml.startsWith("@startuml"), "UML diagram should start with @startuml");
        assertTrue(uml.contains("class Zoo"), "UML diagram should contain class Zoo");
        assertTrue(uml.contains("class Dog"), "UML diagram should contain class Dog");
        assertTrue(uml.contains("class Cat"), "UML diagram should contain class Cat");
        assertTrue(uml.contains("interface Flyable"), "UML diagram should contain interface Flyable");
        assertTrue(uml.contains("class Bird"), "UML diagram should contain class Bird");
        assertTrue(uml.contains("@enduml"), "UML diagram should end with @enduml");
    }
    
    @Test
    public void testMultiClass() throws URISyntaxException, IOException {
        String uml = generateUML("MultiClass.java");
        assertTrue(uml.startsWith("@startuml"), "UML diagram should start with @startuml");
        assertTrue(uml.contains("class A"), "UML diagram should contain class A");
        assertTrue(uml.contains("@enduml"), "UML diagram should end with @enduml");
        // A
        assertTrue(uml.contains("+ getName(): String"));
        assertTrue(uml.contains("+ getAge(): int"));
        
        // B
        String expectedB = "class B {\n" +
                            "    - name: String\n" +
                            "    + gender: String\n" +
                            "    + doSomething(): void\n" +
                            "}";
        assertTrue(uml.contains(expectedB), "UML diagram should contain class B");

        // C
        String expectedC = "class C {\n" +
                            "    + run(): void\n" +
                            "}";
        assertTrue(uml.contains(expectedC), "UML diagram should contain class C");
    }

    @Test
    public void testMultiInterface() throws URISyntaxException, IOException {   
        String uml = generateUML("MultiInterface.java");
        assertTrue(uml.startsWith("@startuml"), "UML diagram should start with @startuml");
        
        String expectedClass = "class MultiInterface {\n" +
                                "    + {static} interface_count: int\n" +
                                "}";
        assertTrue(uml.contains(expectedClass), "UML diagram should contain class MultiInterface");

        String expectedInterfaceA = "interface InterfaceA {\n" +
                                    "    + doA(): void\n" +
                                    "}";
        assertTrue(uml.contains(expectedInterfaceA), "UML diagram should contain interface InterfaceA");

        String expectedInterfaceB = "interface InterfaceB {\n" +
                                    "    + doB1(): void\n" +
                                    "    + doB2(): void\n" +
                                    "}";
        assertTrue(uml.contains(expectedInterfaceB), "UML diagram should contain interface InterfaceB");
        
        assertTrue(uml.contains("@enduml"), "UML diagram should end with @enduml");
    }
}
