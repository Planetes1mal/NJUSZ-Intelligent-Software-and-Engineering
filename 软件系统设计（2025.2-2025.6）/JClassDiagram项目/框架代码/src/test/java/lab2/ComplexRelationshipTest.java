package lab2;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import diagram.ClassDiagram;
import diagram.ClassDiagramGenerator;

public class ComplexRelationshipTest {
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

    public static int countSubstring(String str, String sub) {
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

    @Test
    public void testSimpleassociation() throws URISyntaxException, IOException {
        // 获取Animal.java文件的URL
        String uml = generateUML("SimpleAssociation.java");
        // 断言UML图是否以@startuml开头
        assertTrue(uml.startsWith("@startuml"), "UML diagram should contain @startuml");
        // 断言UML图是否包含class A
        assertTrue(uml.contains("class A"), "UML diagram should contain class A");
        // 断言UML图是否包含class B
        assertTrue(uml.contains("class B"), "UML diagram should contain class B");
        // 定义期望的UML图内容
        String expected = "class C {\n" +
                "    ~ a: A\n" +
                "}";
        // 断言UML图是否包含class C
        assertTrue(uml.contains(expected), "UML diagram should contain class C");
        // 断言UML图是否包含A <|-- B
        assertTrue(uml.contains("A <|-- B"), "UML diagram should contain A <|-- B");
        // 断言UML图是否包含A <-- C
        assertTrue(uml.contains("A <-- C"), "UML diagram should contain A <-- C");
        // 断言UML图是否以@enduml结尾
        assertTrue(uml.contains("@enduml"), "UML diagram should contain @enduml");
    }

    @Test
    public void testListassociation() throws URISyntaxException, IOException {
        // 生成UML图
        String uml = generateUML("ListAssociation.java");

        // 验证UML图的基本结构
        assertTrue(uml.startsWith("@startuml"), "UML diagram should start with @startuml");
        assertTrue(uml.contains("@enduml"), "UML diagram should end with @enduml");

        // 验证类A的存在及其属性
        String expectedA = "class A {\n" +
                "    + b: List<B>\n" +
                "}";
        assertTrue(uml.contains("+ b: List<B>"), "UML diagram should contain attribute b in class A");
        assertTrue(uml.contains(expectedA), "UML diagram should contain class A");

        // 验证类B的存在及其属性
        String expectedB = "class B {\n" +
                "    - d: D\n" +
                "    # c0: C\n" +
                "    + c: C\n" +
                "}";
        assertTrue(uml.contains("- d: D"), "UML diagram should contain attribute d in class B");
        assertTrue(uml.contains("# c0: C"), "UML diagram should contain attribute c0 in class B");
        assertTrue(uml.contains("+ c: C"), "UML diagram should contain attribute c in class B");
        assertTrue(uml.contains(expectedB), "UML diagram should contain class B");

        // 验证类C和类D的存在
        assertTrue(uml.contains("class C"), "UML diagram should contain class C");
        assertTrue(uml.contains("class D"), "UML diagram should contain class D");

        // 验证关联关系
        assertTrue(uml.contains("B <-- A"), "UML diagram should contain B <-- A");
        assertTrue(uml.contains("C <-- B"), "UML diagram should contain C <-- B");
        assertTrue(uml.contains("D <-- B"), "UML diagram should contain D <-- B");

        // 测试没有多余的边
        assertEquals(1, countSubstring(uml, "C <-- B"));
    }

    @Test
    public void testBookLibraryPeopleReadable() throws URISyntaxException, IOException {
        // 生成UML图
        String uml = generateUML("Library.java");

        // 验证基础结构
        assertTrue(uml.startsWith("@startuml"), "UML diagram should start with @startuml");
        assertTrue(uml.contains("@enduml"), "UML diagram should end with @enduml");

        // 验证Book类存在及其结构
        String expectedBook = "class Book {\n" +
                "    ~ content: String\n" +
                "    + getContent(): String\n" +
                "}";
        assertTrue(uml.contains(expectedBook), "UML diagram should contain Book class with correct structure");

        // 验证People类存在及其关联字段
        String expectedPeople = "class People {\n" +
                "    ~ things: Readable[]\n" +
                "}";
        assertTrue(uml.contains(expectedPeople), "UML diagram should contain People class with Readable array");

        String expectedLibrary = "class Library {\n" +
                "    ~ books: Map<Integer, Book>\n" +
                "    ~ getBook(id: int): Book\n" +
                "}";
        assertTrue(uml.contains(expectedLibrary), "UML diagram should contain Libarary class with Map structure");

        // 验证Readable接口存在及方法
        String expectedInterface = "interface Readable {\n" +
                "    + getContent(): String\n" +
                "}";
        assertTrue(uml.contains(expectedInterface), "UML diagram should contain Readable interface");

        // 验证关联关系
        assertTrue(uml.contains("Book <-- Library"), "UML should contain Book <-- Library");
        assertTrue(uml.contains("Readable <-- People"), "UML should contain Readable <-- People");

        // 验证接口实现关系
        assertTrue(uml.contains("Readable <|.. Book"), "UML should show Book implements Readable");
    }

    @Test
    public void testLibraryStructure() throws URISyntaxException, IOException {
        // 生成Library2相关UML图
        String uml = generateUML("Library2.java");

        // 验证UML基础结构
        assertTrue(uml.startsWith("@startuml"), "UML diagram should start with @startuml");
        assertTrue(uml.contains("@enduml"), "UML diagram should end with @enduml");

        // 验证Library2类结构
        String expectedLibrary = "class Library2 {\n"
                + "    - managers: ArrayList<Manager>\n"
                + "    # friendLibrary: Library2\n"
                + "    + records: Map<Student, List<Book>>\n"
                + "}";
        assertTrue(uml.contains(expectedLibrary), "Library2 should contain correct attributes");

        // 验证Book类结构
        String expectedBook = "class Book {\n"
                + "    - price: Integer\n"
                + "    ~ pages: Page[][]\n"
                + "    + getPrice(): int\n"
                + "}";
        assertTrue(uml.contains(expectedBook), "Book should contain correct attributes and methods");

        // 验证Page类结构
        String expectedPage = "class Page {\n"
                + "    + content: String\n"
                + "}";
        assertTrue(uml.contains(expectedPage), "Page should contain content attribute");

        // 验证Student类结构
        String expectedStudent = "class Student {\n"
                + "    - age: int\n"
                + "    + name: String\n"
                + "}";
        assertTrue(uml.contains(expectedStudent), "Student should contain age and name attributes");

        // 验证Manager空类结构
        assertTrue(uml.contains("class Manager {\n}"), "Manager should be an empty class");

        // 验证关联关系
        assertTrue(uml.contains("Student <-- Library2"), "Student should associate to Library2");
        assertTrue(uml.contains("Book <-- Library2"), "Book should associate to Library2");
        assertTrue(uml.contains("Manager <-- Library2"), "Manager should associate to Library2");
        assertTrue(uml.contains("Page <-- Book"), "Page should associate to Book");

        // 确保关系线唯一性
        assertEquals(1, countSubstring(uml, "Student <-- Library2"), "Student-Library2 relation should appear once");
        assertEquals(1, countSubstring(uml, "Page <-- Book"), "Page-Book relation should appear once");
    }

    @Test
    public void testDependencies() throws URISyntaxException, IOException {
        // 生成UML图内容
        String uml = generateUML("OnlineJudger.java");

        // 验证UML基础结构
        assertTrue(uml.startsWith("@startuml"), "UML diagram should start with @startuml");
        assertTrue(uml.contains("@enduml"), "UML diagram should end with @enduml");

        // 验证PaperResult类结构
        String expectedPaperResult = "class PaperResult {\n"
                + "    + res: String\n"
                + "}";
        assertTrue(uml.contains(expectedPaperResult), "PaperResult should have public string attribute");

        // 验证Calculator类方法签名
        String expectedCalculator = "class Calculator {\n"
                + "    + add(a: int, b: int): int\n"
                + "}";
        assertTrue(uml.contains(expectedCalculator), "Calculator should have correct add method");

        // 验证Paper类保护属性
        String expectedPaper = "class Paper {\n"
                + "    ~ paper: String\n"
                + "}";
        assertTrue(uml.contains(expectedPaper), "Paper should have package-private attribute");

        // 验证OnlineJudger类方法参数
        String expectedJudger = "class OnlineJudger {\n"
                + "    + judge(paper: Paper): PaperResult\n"
                + "}";
        assertTrue(uml.contains(expectedJudger), "OnlineJudger should have method with Paper parameter");

        // 验证依赖箭头方向
        assertTrue(uml.contains("PaperResult <.. OnlineJudger"),
                "Should have dependency from OnlineJudger to PaperResult");
        assertTrue(uml.contains("Paper <.. OnlineJudger"),
                "Should have dependency from OnlineJudger to Paper");

        // 防止重复生成依赖线
        assertEquals(1, countSubstring(uml, "PaperResult <.. OnlineJudger"),
                "PaperResult dependency should be unique");
        assertEquals(1, countSubstring(uml, "Paper <.. OnlineJudger"),
                "Paper dependency should be unique");
    }

    @Test
    public void testDependencies2() throws URISyntaxException, IOException {
        // 生成UML图内容
        String uml = generateUML("Calculator.java");
        
        // 验证UML基础结构
        assertTrue(uml.startsWith("@startuml"), "UML diagram should start with @startuml");
        assertTrue(uml.contains("@enduml"), "UML diagram should end with @enduml");

        // 验证PaperResult类结构
        String expectedPaperResult = "class PaperResult {\n"
                + "    + res: String\n"
                + "}";
        assertTrue(uml.contains(expectedPaperResult), "PaperResult should have public string attribute");

        // 验证OnlineJudger2类方法签名
        String expectedJudger = "class OnlineJudger2 {\n"
                + "    # getPapers(): List<Paper>\n"
                + "    + judge(id: int, papers: List<Paper>): String\n"
                + "}";
        assertTrue(uml.contains(expectedJudger), "OnlineJudger2 should have correct method signatures");

        // 验证Calculator类方法参数
        String expectedCalculator = "class Calculator {\n"
                + "    + add(a: int, b: int): int\n"
                + "}";
        assertTrue(uml.contains(expectedCalculator), "Calculator should have int parameters");

        // 验证Paper类可见性修饰符
        String expectedPaper = "class Paper {\n"
                + "    ~ paper: String\n"
                + "}";
        assertTrue(uml.contains(expectedPaper), "Paper should use package-private modifier");

        // 验证依赖方向及语法
        assertTrue(uml.contains("Paper <.. OnlineJudger2"), 
                "Dependency should point from OnlineJudger2 to Paper");
        assertTrue(uml.contains("Calculator <.. OnlineJudger2"), 
                "Dependency should point from OnlineJudger2 to Calculator");

        // 防止重复依赖线
        assertEquals(1, countSubstring(uml, "Paper <.. OnlineJudger2"), 
                "Paper dependency should occur exactly once");
        assertEquals(1, countSubstring(uml, "Calculator <.. OnlineJudger2"), 
                "Calculator dependency should occur exactly once");
    }

    @Test
    public void testComplexDependence() throws URISyntaxException, IOException {
        // 生成UML图内容
        String uml = generateUML("ComplexDependence.java");

        // 验证基础结构
        assertTrue(uml.startsWith("@startuml"), "Missing PlantUML start tag");
        assertTrue(uml.contains("@enduml"), "Missing PlantUML end tag");

        // 验证类A结构
        String expectedA = "class A {\n"
                + "    + b: B\n"
                + "    + createB(): B\n"
                + "}";
        assertTrue(uml.contains(expectedA), "Class A structure mismatch");

        // 验证类B结构
        String expectedB = "class B {\n"
                + "    - id: int\n"
                + "    + a: A\n"
                + "    + getId(): int\n"
                + "}";
        assertTrue(uml.contains(expectedB), "Class B structure mismatch");

        // 验证接口C定义
        String expectedC = "interface C {\n"
                + "    + doSomething(): void\n"
                + "}";
        assertTrue(uml.contains(expectedC), "Interface C definition incorrect");

        // 验证类D方法签名
        assertTrue(uml.contains("class D {\n    + doSomething(): void\n}"), 
                "Class D should have void method");

        // 验证类E的双重方法
        String expectedE = "class E {\n"
                + "    # bye(): void\n"
                + "    + hello(): void\n"
                + "}";
        assertTrue(uml.contains(expectedE), "Class E method visibility error");

        // 验证类F参数化方法
        String expectedF = "class F {\n"
                + "    - isBigE(e: E): int\n"
                + "    + getE(): E\n"
                + "}";
        assertTrue(uml.contains(expectedF), "Class F method parameters incorrect");

        // 验证关联关系
        assertTrue(uml.contains("B <-- A"), "Missing B association to A");
        assertTrue(uml.contains("A <-- B"), "Missing A association to B");

        // 验证接口实现关系
        assertTrue(uml.contains("C <|.. D"), "D should implement interface C");

        // 验证依赖关系
        assertTrue(uml.contains("C <.. E"), "Missing E dependency to C");
        assertTrue(uml.contains("E <.. F"), "Missing F dependency to E");

        // 验证关系唯一性
        assertEquals(1, countSubstring(uml, "C <|.. D"), 
                "Interface implementation should be unique");
        assertEquals(1, countSubstring(uml, "E <.. F"), 
                "Dependency relationship duplication");
    }

    @Test
    public void testSchoolStructure() throws URISyntaxException, IOException {
        // 生成UML图内容
        String uml = generateUML("School.java");

        // 验证基础结构
        assertTrue(uml.startsWith("@startuml"), "UML should start with @startuml");
        assertTrue(uml.contains("@enduml"), "UML should end with @enduml");

        // 验证Group类方法签名
        String expectedGroup = "class Group {\n"
                + "    - removeStudent(student: Student): void\n"
                + "    + addStudent(student: Student): void\n"
                + "}";
        assertTrue(uml.contains(expectedGroup), "Group class methods mismatch");

        // 验证School类复杂泛型属性
        String expectedSchool = "class School {\n"
                + "    + table: Map<Grade, List<Map<Group, List<Student>>>>\n"
                + "    + addStudent(student: Student): void\n"
                + "}";
        assertTrue(uml.contains(expectedSchool), "School's complex generic structure incorrect");

        // 验证SmartStudent继承关系
        assertTrue(uml.contains("class SmartStudent {\n    + study(): void\n}"), 
                "SmartStudent missing study method");
        assertTrue(uml.contains("Student <|-- SmartStudent"), 
                "Inheritance relationship missing");

        // 验证Grade类属性
        assertTrue(uml.contains("class Grade {\n    + level: int\n}"), 
                "Grade level attribute error");

        // 验证Student可见性修饰符
        String expectedStudent = "class Student {\n"
                + "    - name: String\n"
                + "    + age: int\n"
                + "}";
        assertTrue(uml.contains(expectedStudent), "Student visibility modifiers incorrect");

        // 验证关联关系
        assertTrue(uml.contains("Grade <-- School"), "Grade association missing");
        assertTrue(uml.contains("Group <-- School"), "Group association missing");
        assertTrue(uml.contains("Student <-- School"), "Student association missing");

        // 验证依赖关系
        assertTrue(uml.contains("Student <.. Group"), 
                "Group should depend on Student");

        // 验证关系唯一性
        assertEquals(1, countSubstring(uml, "Student <|-- SmartStudent"), 
                "Multiple inheritance lines detected");
        assertEquals(1, countSubstring(uml, "Grade <-- School"), 
                "Duplicate grade association");
        assertEquals(1, countSubstring(uml, "Student <-- School"), 
                "Duplicate grade association");
        assertEquals(1, countSubstring(uml, "Student <.. Group"), 
                "Duplicate grade association");
        assertFalse(uml.contains("Student <.. School"), "Student association missing");
    }

    @Test
    public void testTreeNodeRelationships() throws URISyntaxException, IOException {
        // 生成UML图内容
        String uml = generateUML("Node.java");

        // 验证基础结构
        assertTrue(uml.startsWith("@startuml"), "Missing PlantUML start tag");
        assertTrue(uml.contains("@enduml"), "Missing PlantUML end tag");

        // 验证Node类结构
        String expectedNode = "class Node {\n"
                + "    + data: int\n"
                + "    + next: Node\n"
                + "}";
        assertTrue(uml.contains(expectedNode), "Node class structure mismatch");

        // 验证GroupNode类方法签名
        String expectedGroupNode = "class GroupNode {\n"
                + "    + children: List<TreeNode>\n"
                + "    # addChildren(list: LinkList): void\n"
                + "    + getChildNum(): int\n"
                + "}";
        assertTrue(uml.contains(expectedGroupNode), "GroupNode method parameters incorrect");

        // 验证LinkList可见性修饰符
        assertTrue(uml.contains("class LinkList {\n    ~ head: Node\n}"), 
                "LinkList should have package-private head");

        // 验证接口实现关系
        String expectedInterface = "interface TreeNode {\n    + getChildNum(): int\n}";
        assertTrue(uml.contains(expectedInterface), "TreeNode interface definition missing");
        assertTrue(uml.contains("TreeNode <|.. RedNode"), 
                "RedNode should implement TreeNode");
        assertTrue(uml.contains("TreeNode <|.. GroupNode"), 
                "GroupNode should implement TreeNode");

        // 验证RedNode方法签名
        assertTrue(uml.contains("class RedNode {\n    + getChildNum(): int\n}"), 
                "RedNode method signature error");

        // 验证关联关系方向
        assertTrue(uml.contains("Node <-- LinkList"), 
                "Missing Node association to LinkList");
        assertTrue(uml.contains("TreeNode <-- GroupNode"), 
                "Missing TreeNode association to GroupNode");

        // 验证依赖关系语法
        assertTrue(uml.contains("LinkList <.. GroupNode"), 
                "GroupNode should depend on LinkList");

        // 防止重复关系线
        assertEquals(1, countSubstring(uml, "TreeNode <|.. RedNode"), 
                "Multiple interface implementations detected");
        assertEquals(1, countSubstring(uml, "Node <-- LinkList"), 
                "Duplicate node association lines");
    }
}
