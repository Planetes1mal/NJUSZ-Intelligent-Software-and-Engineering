package lab3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import diagram.ClassDiagramGenerator;
import diagram.ClassDiagram;

public class MultiFileTest {
    private static int countSubstring(String str, String sub) {
        if (str == null || sub == null || str.isEmpty() || sub.isEmpty()) {
            return 0;
        }
        int count = 0;
        int index = 0;
        while ((index = str.indexOf(sub, index)) != -1) {
            count++;
            index += sub.length(); // 移动到下一个位置继续查找
        }
        return count;
    }

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
    public void testSimpleMultiFile() throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource("animal");
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        String uml = diagram.generateUML();
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
        assertTrue(uml.contains("setName(name: String): void"),
                "UML diagram should contain setName(name: String): void");
        assertTrue(uml.contains("fly(): void"), "UML diagram should contain fly(): void");
        assertTrue(uml.contains("wolf(): void"), "UML diagram should contain wolf(): void");
        // 测试类中字段存在
        assertTrue(uml.contains("- name: String"), "UML diagram should contain - name: String");
        assertTrue(uml.contains("+ {static} count: int"), "UML diagram should contain + {static} count: int");
        assertTrue(uml.contains("+ gender: String"), "UML diagram should contain + gender: String");
        assertTrue(uml.contains("+ color: String"), "UML diagram should contain + color: String");

        String expected = "class Bird {\n" +
                "    + color: String\n" +
                "    + fly(): void\n" +
                "}";
        assertTrue(uml.contains(expected), "class Bird information is incorrect");
    }

    @Test
    public void testBPlusTree() throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource("b_plus_tree");
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        String uml = diagram.generateUML();
        // 测试PlantUML文本
        assertTrue(uml.startsWith("@startuml"), "UML diagram should contain @startuml");
        // 测试InternalNode类（复杂泛型）
        assertTrue(uml.contains("""
                class InternalNode<K extends Comparable<K>, V> {
                    - children: Node<K, V>[]
                    - findChildIndex(key: K): int
                """) || uml.contains("""
                class InternalNode<K extends Comparable<K>, V> {
                    - findChildIndex(key: K): int
                    - children: Node<K, V>[]
                """), "InternalNode information is incorrect");
        // 抽象类识别
        testExsitUML(uml, "abstract class Node<K extends Comparable<K>, V> {");
        testExsitUML(uml, "+ {abstract} insert(key: K, value: V): SplitResult<K, V>");
        testExsitUML(uml, "# entries: Entry<K, V>[]");
        testExsitUML(uml, "+ insert(key: K, value: V): SplitResult<K, V>");
        // 测试boolean类型
        testExsitUML(uml, "+ isLeaf(): boolean");
        // 测试接口存在
        assertTrue(uml.contains("interface SiblingLink<K extends Comparable<K>, V> {") || uml.contains("interface SiblingLink {"), "SiblingLink information is incorrect");
        
        assertTrue(uml.contains("""
            class SplitResult<K extends Comparable<K>, V> {
                - midKey: K
                - newNode: Node<K, V>
            }
            """) || uml.contains("""
            class SplitResult<K extends Comparable<K>, V> {
                - newNode: Node<K, V>
                - midKey: K
            }
            """), "SplitResult information is incorrect");

        // 测试复杂关系
        assertEquals(1, countSubstring(uml, "Node <-- BPlusTree"), "Node <-- BPlusTree should appear once");
        assertEquals(1, countSubstring(uml, "SplitResult <.. BPlusTree"), "SplitResult <.. BPlusTree should appear once");
        assertEquals(1, countSubstring(uml, "InternalNode <.. BPlusTree"), "InternalNode <.. BPlusTree should appear once");
        assertEquals(1, countSubstring(uml, "Entry <-- Node"), "Entry <-- Node should appear once");
        assertEquals(1, countSubstring(uml, "SplitResult <.. Node"), "SplitResult <.. Node should appear once");
        assertEquals(1, countSubstring(uml, "LeafNode <.. SiblingLink"), "LeafNode <.. SiblingLink should appear once");
        assertEquals(1, countSubstring(uml, "Node <|-- InternalNode"), "Node <|-- InternalNode should appear once");
        assertEquals(1, countSubstring(uml, "Node <-- InternalNode"), "Node <-- InternalNode should appear once");
        assertEquals(1, countSubstring(uml, "SplitResult <.. InternalNode"), "SplitResult <.. InternalNode should appear once");
        assertEquals(1, countSubstring(uml, "Node <|-- LeafNode"), "Node <|-- LeafNode should appear once");
        assertEquals(1, countSubstring(uml, "SiblingLink <|.. LeafNode"), "SiblingLink <|.. LeafNode should appear once");
        assertEquals(1, countSubstring(uml, "SplitResult <.. LeafNode"), "SplitResult <.. LeafNode should appear once");
        assertEquals(1, countSubstring(uml, "Node <-- SplitResult"), "Node <-- SplitResult should appear once"); 
        assertEquals(0, countSubstring(uml, "Node <.. SplitResult"), "Node <-- SplitResult should not appear");

        List<String> codeSmells = diagram.getCodeSmells();
        assertTrue(codeSmells.contains("Lazy Class: Entry"), "Entry should be a lazy class");
        assertTrue(codeSmells.contains("Lazy Class: SplitResult"), "SplitResult should be a lazy class");

        // 测试设计模式
        assertFalse(codeSmells.contains("Possible Design Patterns: Singleton Pattern"),
                "BPlusTree should not have Singleton Pattern");
        assertFalse(codeSmells.contains("Possible Design Patterns: Strategy Pattern"),
                "BPlusTree should not have Strategy Pattern");
        // 测试循环依赖
        assertTrue((containsCycle(codeSmells, "SplitResult <.. Node <.. SplitResult") || containsCycle(codeSmells, "LeafNode <.. SiblingLink <.. LeafNode")),
                "BPlusTree should have circular dependency: SplitResult <.. Node <.. SplitResult");
    }
}
