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

public class RelationshipTest {

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
    public void testInheritance() throws URISyntaxException, IOException {
        String uml = generateUML("Inheritance.java");
        assertTrue(uml.startsWith("@startuml"), "UML diagram should start with @startuml");
        assertTrue(uml.contains("@enduml"), "UML diagram should end with @enduml");

        String expectedAnimal = "class Animal {\n" +
                                "    + name: String\n" +
                                "    + eat(): void\n" +
                                "}";
        assertTrue(uml.contains(expectedAnimal), "UML diagram should contain class Animal");
        String expectedSpecialDog = "class SpecialDog {\n" +
                                    "    + specialBark(): void\n" +
                                    "}";
        assertTrue(uml.contains(expectedSpecialDog), "UML diagram should contain class SpecialDog");

        assertTrue(uml.contains("class Cat"));
        assertTrue(uml.contains("class Dog"));

        assertTrue(uml.contains("+ meow(): void"));

        assertTrue(uml.contains("Animal <|-- Dog"));
        assertTrue(uml.contains("Animal <|-- Cat"));
        assertTrue(uml.contains("Dog <|-- SpecialDog"));
    }

    @Test
    public void testImplement() throws URISyntaxException, IOException {
        String uml = generateUML("Implement.java");
        assertTrue(uml.startsWith("@startuml"), "UML diagram should start with @startuml");
        assertTrue(uml.contains("@enduml"), "UML diagram should end with @enduml");

        String expectedMove = "class Move {\n" +
                              "    + execute(): void\n" +
                              "}";
        assertTrue(uml.contains(expectedMove), "UML diagram should contain class Move");

        String expectedBranchInstruction = "interface Instruction {\n" +
                                            "    + execute(): void\n" +
                                            "}";
        assertTrue(uml.contains(expectedBranchInstruction), "UML diagram should contain interface Instruction");

        assertTrue(uml.contains("class BranchInstruction"));
        assertTrue(uml.contains("- rs1: int"));
        assertTrue(uml.contains("- rs2: int"));
        assertTrue(uml.contains("- imm: int"));
        assertTrue(uml.contains("- funct3: int"));
        assertTrue(uml.contains("- signExtend(value: int, bits: int): int"));
        
        assertTrue(uml.contains("Instruction <|.. BranchInstruction"));
        assertTrue(uml.contains("Instruction <|.. Move"));
    }

    @Test
    public void testInterfaceInheritance() throws URISyntaxException, IOException {
        String uml = generateUML("InterfaceInher.java");
        assertTrue(uml.startsWith("@startuml"), "UML diagram should start with @startuml");
        assertTrue(uml.contains("@enduml"), "UML diagram should end with @enduml");
        
        String expectedA = "interface A {\n" +
                            "    + doA(): void\n" +
                            "}";
        assertTrue(uml.contains(expectedA), "UML diagram should contain interface A");

        String expectedB = "interface B {\n" +
                            "    + doB(): void\n" +
                            "}";
        assertTrue(uml.contains(expectedB), "UML diagram should contain interface B");

        String expectedC = "interface C {\n" +
                            "    + doC(): void\n" +
                            "}";
        assertTrue(uml.contains(expectedC), "UML diagram should contain interface C");  

        String expectedD = "interface D {\n" +
                            "    + doD(): void\n" +
                            "}";
        assertTrue(uml.contains(expectedD), "UML diagram should contain interface D");

        assertTrue(uml.contains("A <|-- B"), "A should extend B");
        assertTrue(uml.contains("A <|-- C"), "A should extend C");
        assertTrue(uml.contains("B <|-- D"), "B should extend D");
        assertTrue(uml.contains("C <|-- D"), "C should extend D");
    }

    @Test
    public void testProduct() throws URISyntaxException, IOException {
        String uml = generateUML("Product.java");
        assertTrue(uml.startsWith("@startuml"), "UML diagram should start with @startuml");
        assertTrue(uml.contains("@enduml"), "UML diagram should end with @enduml");
        
        assertTrue(uml.contains("class Product"), "UML diagram should contain class Product");
        assertTrue(uml.contains("- productId: int"));
        assertTrue(uml.contains("- productName: String"));
        assertTrue(uml.contains("- price: double"));
        assertTrue(uml.contains("- stockQuantity: int"));

        assertTrue(uml.contains("class DiscountedProduct"), "UML diagram should contain class DiscountedProduct");
        assertTrue(uml.contains("- discountRate: double"));
        
        assertTrue(uml.contains("interface InventoryManageable"), "UML diagram should contain interface InventoryManageable");
        assertTrue(uml.contains("interface Displayable"), "UML diagram should contain interface Displayable");

        assertTrue(uml.contains("Product <|-- DiscountedProduct"));
        assertTrue(uml.contains("InventoryManageable <|.. Product"));
        assertTrue(uml.contains("Displayable <|.. Product"));
    }
}
