package lab3;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import diagram.ClassDiagram;
import diagram.ClassDiagramGenerator;

public class ConfigTest {
    private void testExsitUML(String uml, String umlStr) {
        assertTrue(uml.contains(umlStr), "Missing " + umlStr);
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
                for (int i = 1, index = (tar.indexOf(cycle.get(0)) + 1)
                        % n; i <= n; i++, index = (index + 1) % n) {
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
    public void testConfig() throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource("Duck.java");
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        // 加载配置文件
        URL configUrl = getClass().getResource("config_duck.xml");
        Path configPath = null;
        if (configUrl != null) {
            configPath = Paths.get(configUrl.toURI());
        } else {
            throw new IOException("Config file not found");
        }
        diagram.loadConfig(configPath.toString());

        // 测试生成的UML是否包含指定内容
        String uml = diagram.generateUML();
        testExsitUML(uml, """
                class FlyWithWings {
                    + fly(): void
                }
                """);
        testExsitUML(uml, """
                class RedDuck {
                    ~ farmer: Farmer
                    + display(): void
                }
                """);

        // 测试生成的UML是否包含下列关系
        testExsitUML(uml, "FlyBehavior <-- Duck");
        testExsitUML(uml, "QuackBehavior <-- Duck");
        testExsitUML(uml, "QuackBehavior <|.. Quack");
        testExsitUML(uml, "QuackBehavior <|.. Squeak");
        testExsitUML(uml, "QuackBehavior <|.. MuteQuack");
        testExsitUML(uml, "FlyBehavior <|.. FlyWithWings");
        testExsitUML(uml, "FlyBehavior <|.. FlyNoWay");
        testExsitUML(uml, "Duck <|-- RedDuck");
        testExsitUML(uml, "Farmer <-- RedDuck");
        testExsitUML(uml, "Duck <|-- BlueDuck");
        testExsitUML(uml, "Duck <|-- RubberDuck");
        testExsitUML(uml, "Duck <|-- GreenDuck");
        testExsitUML(uml, "Duck <|-- GoodDuck");
        testExsitUML(uml, "Duck <|-- BadDuck");
        testExsitUML(uml, "Duck <|-- NewDuck");
        testExsitUML(uml, "Duck <|-- HappyDuck");
        testExsitUML(uml, "Duck <|-- AngryDuck");
        testExsitUML(uml, "Duck <|-- SadDuck");
        testExsitUML(uml, "Duck <|-- BigDuck");
        testExsitUML(uml, "Duck <|-- SmallDuck");
        testExsitUML(uml, "Duck <.. Farmer");
        testExsitUML(uml, "RedDuck <.. Farmer");

        List<String> logs = diagram.getCodeSmells();

        assertTrue(logs.contains("Lazy Class: GreenDuck"), "GreenDuck should be a lazy class");
        assertTrue(logs.contains("Lazy Class: SadDuck"), "SadDuck should be a lazy class");
        assertTrue(logs.contains("Lazy Class: GoodDuck"), "GoodDuck should be a lazy class");

        assertTrue(logs.contains("Possible Design Patterns: Strategy Pattern"), "Strategy Pattern should exist");
        assertTrue(containsCycle(logs, "RedDuck <.. Farmer <.. RedDuck"), "RedDuck and Farmer should not have a circular dependency");

        assertFalse(logs.contains("Too Many Children: Duck"), "Too Many Children should not exist");
    }

    @Test
    public void testEmptyConfig() throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource("RegPool.java");
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        // 加载配置文件
        URL configUrl = getClass().getResource("config_empty.xml");
        Path configPath = null;
        if (configUrl != null) {
            configPath = Paths.get(configUrl.toURI());
        } else {
            throw new IOException("Config file not found");
        }
        diagram.loadConfig(configPath.toString());

        List<String> logs = diagram.getCodeSmells();
        assertTrue(logs.size() == 0, "No code smells should be found");
    }
}
