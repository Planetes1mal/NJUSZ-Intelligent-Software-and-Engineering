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

public class ClassAnalysisTest {
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
    public void testGodClass() throws URISyntaxException, IOException {
        List<String> logs = getCodeSmells("GodClass.java");

        assertTrue(logs.contains("God Class: Alphabet"), "Your output should contain God Class: Alphabet");
        assertTrue(logs.contains("God Class: God"), "Your output should contain God Class: God");
    }

    @Test
    public void testLazyClass() throws URISyntaxException, IOException {
        List<String> logs = getCodeSmells("LazyClass.java");

        assertTrue(logs.size() == 3, "Unexpectly extra code smell");

        assertTrue(logs.contains("Lazy Class: Empty"), "Your output should contain Lazy Class: Empty");
        assertTrue(logs.contains("Lazy Class: Rectangle"), "Your output should contain Lazy Class: Rectangle");
        assertTrue(logs.contains("Lazy Class: Add"), "Your output should contain Lazy Class: Add");
    }

    @Test
    public void testDataClass() throws URISyntaxException, IOException {
        List<String> logs = getCodeSmells("DataClass.java");

        assertTrue(logs.size() == 2, "Unexpectly extra code smell");

        assertTrue(logs.contains("Data Class: Car"), "Your output should contain Data Class: Car");
        assertTrue(logs.contains("Data Class: Data"), "Your output should contain Data Class: Data");
    }
}
