package lab3;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import diagram.ClassDiagram;
import diagram.ClassDiagramGenerator;

public class DesignPatternTest {
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

    public static int countSubstring(String str, String sub) {
        if (str == null || sub == null || str.isEmpty() || sub.isEmpty()) {
            return 0;
        }
        int count = 0;
        int index = 0;
        while ((index = str.indexOf(sub, index)) != -1) {
            count++;
            index += sub.length();
        }
        return count;
    }

    public void testExsitUML(String uml, String umlStr) {
        assertTrue(uml.contains(umlStr), "Missing " + umlStr);
    }

    public void testNotExsitUML(String uml, String umlStr) {
        assertFalse(uml.contains(umlStr), umlStr + "should not exist.");
    }

    @Test
    public void testRiscV() throws URISyntaxException, IOException {
        List<String> logs = getCodeSmells("RiscVMachine.java");

        assertTrue(logs.contains("Possible Design Patterns: Singleton Pattern"), "Singleton Pattern should exist");
        assertTrue(!logs.contains("Possible Design Patterns: Strategy Pattern"), "Strategy Pattern should not exist");
    }

    @Test
    public void testRV32Machine2() throws URISyntaxException, IOException {
        List<String> logs = getCodeSmells("RV32Machine2.java");
        
        assertTrue(!logs.contains("Possible Design Patterns: Singleton Pattern"), "Singleton Pattern should not exist");
        assertTrue(logs.contains("Possible Design Patterns: Strategy Pattern"), "Strategy Pattern should exist");
    }
}
