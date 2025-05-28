package lab2;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import diagram.ClassDiagram;
import diagram.ClassDiagramGenerator;

public class ExtendAnalysisTest {
    public List<String> getCodeSmells(String testcase) throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource(testcase);
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        return diagram.getCodeSmells();
    }

    @Test
    public void testSimpleTree() throws URISyntaxException, IOException {
        List<String> logs = getCodeSmells("SimpleInheritanceTree.java");

        assertTrue(logs.size() == 2, "Unexpectly extra code smell");

        assertTrue(logs.contains("Too Many Children: Parent"), "Your output should contain Too Many Children: Parent");
        assertTrue(logs.contains("Inheritance Abuse: A <|-- B <|-- C <|-- D <|-- E <|-- F"),
                "Your output should contain Inheritance Abuse: A <|-- B <|-- C <|-- D <|-- E <|-- F");
    }

    @Test
    public void testComplexTree() throws URISyntaxException, IOException {
        List<String> logs = getCodeSmells("AnimalHierarchy.java");

        assertTrue(logs.size() == 6, "Unexpectly extra code smell");

        assertTrue(logs.contains("Too Many Children: Dog"), "Your output should contain Too Many Children: Dog");
        assertTrue(logs.contains("Too Many Children: FlyingBird"),
                "Your output should contain Too Many Children: FlyingBird");
        assertTrue(logs.contains(
                "Inheritance Abuse: Mammal <|-- Carnivore <|-- Canidae <|-- Dog <|-- Bulldog <|-- EnglishBulldog"),
                "Your output should contain Inheritance Abuse: Mammal <|-- Carnivore <|-- Canidae <|-- Dog <|-- Bulldog <|-- EnglishBulldog");
        assertTrue(logs.contains(
                "Inheritance Abuse: Mammal <|-- Carnivore <|-- Canidae <|-- Wolf <|-- GrayWolf <|-- TimberWolf"),
                "Your output should contain Inheritance Abuse: Mammal <|-- Carnivore <|-- Canidae <|-- Wolf <|-- GrayWolf <|-- TimberWolf");
        assertTrue(logs.contains(
                "Inheritance Abuse: Mammal <|-- Carnivore <|-- Felidae <|-- Tiger <|-- BengalTiger <|-- RoyalBengalTiger"),
                "Your output should contain Inheritance Abuse: Mammal <|-- Carnivore <|-- Felidae <|-- Tiger <|-- BengalTiger <|-- RoyalBengalTiger");
        assertTrue(logs.contains(
                "Inheritance Abuse: Mammal <|-- Carnivore <|-- Felidae <|-- Lion <|-- AfricanLion <|-- MasaiLion"),
                "Your output should contain Inheritance Abuse: Mammal <|-- Carnivore <|-- Felidae <|-- Lion <|-- AfricanLion <|-- MasaiLion");
    }
}
