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

public class MixTest {
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
    public void testAnimal() throws URISyntaxException, IOException {
        // 获取Animal.java文件的URL
        String uml = generateUML("Animal.java");
        assertTrue(uml.startsWith("@startuml"));
        assertTrue(uml.contains("class Animal"), "UML diagram should contain class Animal");
        assertTrue(uml.contains("class Bird"), "UML diagram should contain class Bird");
        assertTrue(uml.contains("class Dog"), "UML diagram should contain class Dog");
        
        // 测试继承关系
        assertTrue(uml.contains("Animal <|-- Dog"), "UML diagram should contain Animal <|-- Dog");
        assertTrue(uml.contains("Animal <|-- Bird"), "UML diagram should contain Animal <|-- Bird");
        // 测试接口实现关系
        assertTrue(uml.contains("Flyable <|.. Bird"), "UML diagram should contain Flyable <|.. Bird");
        // 测试类中方法存在
        assertTrue(uml.contains("getName(): String"), "UML diagram should contain getName(): String");
        assertTrue(uml.contains("setName(name: String): void"), "UML diagram should contain setName(name: String): void");
        assertTrue(uml.contains("fly(): void"));
        assertTrue(uml.contains("wolf(): void"));
        // 测试类中字段存在
        assertTrue(uml.contains("- name: String"));
        assertTrue(uml.contains("+ {static} count: int"));
        assertTrue(uml.contains("+ gender: String"));
        assertTrue(uml.contains("+ color: String"));
        
        String expected = "class Bird {\n" +
                          "    + color: String\n" +
                          "    + fly(): void\n" +
                          "}";
        assertTrue(uml.contains(expected));
    }
}
