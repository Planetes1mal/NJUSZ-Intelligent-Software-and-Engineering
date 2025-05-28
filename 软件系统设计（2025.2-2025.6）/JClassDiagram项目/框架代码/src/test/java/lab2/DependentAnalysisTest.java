package lab2;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import diagram.ClassDiagram;
import diagram.ClassDiagramGenerator;

public class DependentAnalysisTest {
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

    private boolean containsCycle(List<String> logs, String cycleStr) {
        for (String log : logs) {
            if (log.startsWith("Circular Dependency: ")) {
                List<String> cycle = Arrays.asList(log.substring(21).split(" <.. "));
                List<String> tar = new ArrayList<>(Arrays.asList(cycleStr.split(" <.. ")));
                if (cycle.size() != tar.size()) {
                    return false;
                }
                tar.remove(0);
                int n = tar.size();
                for (int i = 1, index = (tar.indexOf(cycle.get(0)) + 1) % n; i <= n; i++, index = (index + 1) % n) {
                    if (!tar.get(index).equals(cycle.get(i))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Test
    public void testSimpleGraph() throws URISyntaxException, IOException {
        List<String> logs = getCodeSmells("SimpleCycle.java");

        assertTrue(logs.size() == 1, "Unexpectly extra code smell");

        assertTrue(containsCycle(logs, "Z <.. Y <.. X <.. Z"),
                "Your output should contain cycle like Z <.. Y <.. X <.. Z");
    }

    @Test
    public void testComplexGraph() throws URISyntaxException, IOException {
        List<String> logs = getCodeSmells("ComplexCycle.java");

        assertTrue(logs.size() == 1, "Unexpectly extra code smell");

        assertTrue(containsCycle(logs, "B <.. D <.. E <.. F <.. B"),
                "Your output should contain cycle like B <.. D <.. E <.. F <.. B");
    }
}
