package lab2;

import diagram.ClassDiagram;
import diagram.ClassDiagramGenerator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoreElementTest {
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
    public void testDataTypeRepresentation() throws Exception {
        String uml = generateUML("DataTypes.java");

        // 基本类型验证
        assertTrue(uml.contains("- intVal: int"));
        assertTrue(uml.contains("- doubleVal: double"));
        assertTrue(uml.contains("- boolVal: boolean"));

        // 包装类型验证
        assertTrue(uml.contains("- integerObj: Integer"));
        assertTrue(uml.contains("- doubleObj: Double"));
        assertTrue(uml.contains("- boolObj: Boolean"));

        // 集合类型验证
        assertTrue(uml.contains("- stringList: List<String>"));
        assertTrue(uml.contains("- intSet: Set<Integer>"));
        assertTrue(uml.contains("- objectMap: Map<String, Object>"));

        // 方法签名验证
        assertTrue(uml.contains("+ processNumbers(a: int, b: Double): Map<Long, Double>"));
        assertTrue(uml.contains("+ convertChars(data: byte[]): List<Character>"));
        assertTrue(uml.contains("+ validate(s: Short, flag: Boolean): boolean"));

        // 参数名称验证
        assertFalse(uml.contains("processNumbers(int, Double)"));
        assertTrue(uml.contains("processNumbers(a: int, b: Double)"));

        // 特殊类型验证
        assertTrue(uml.contains("- charVal: char"));
        assertTrue(uml.contains("- charObj: Character"));
        assertTrue(uml.contains("- text: String"));
    }

    @Test
    public void testAbstractClassDiagram() throws Exception {
        String uml = generateUML("Abstract.java");

        // 抽象类标识验证
        assertTrue(uml.contains("abstract class AbstractShape"));
        assertTrue(uml.contains("abstract class ShapeProcessor"));

        // 抽象方法标记验证
        assertTrue(uml.contains("{abstract} calculateArea(): double"));
        assertTrue(uml.contains("{abstract} processShape(shape: AbstractShape): void"));

        // 继承关系验证
        assertTrue(uml.contains("AbstractShape <|-- Circle"));

        // 字段访问修饰符验证
        assertTrue(uml.contains("# color: String"));       // protected字段
        assertTrue(uml.contains("- radius: double"));       // private字段

        // 方法签名验证
        assertTrue(uml.contains("+ getColor(): String"));  // 父类具体方法
        assertTrue(uml.contains("+ getCircumference(): double")); // 子类特有方法

        // 方法实现验证
        assertTrue(uml.contains("calculateArea(): double")); // 覆盖实现

        // 构造函数排除验证
        assertFalse(uml.contains("AbstractShape("));
        assertFalse(uml.contains("Circle("));
        assertFalse(uml.contains("ShapeProcessor("));
    }

    @Test
    public void testOrderStatusUML() throws Exception {
        String uml = generateUML("OrderStatus.java");

        // 验证基本结构
        assertTrue(uml.contains("enum OrderStatus {"));

        // 枚举常量验证（保持顺序）
        assertTrue(uml.contains("NEW"));
        assertTrue(uml.contains("PROCESSING"));
        assertTrue(uml.contains("SHIPPED"));
        assertTrue(uml.contains("DELIVERED"));
        assertTrue(uml.contains("CANCELLED"));

        // 字段验证
        assertTrue(uml.contains("- statusCode: int"));
        assertTrue(uml.contains("- description: String"));

        // 方法验证
        assertTrue(uml.contains("+ getStatusInfo(): String"));
        assertTrue(uml.contains("{static} fromCode(code: int): OrderStatus"));

    }

    @Test
    public void testGenericContainerStructure() throws Exception {
        String uml = generateUML("GenericContainer.java");

        // 验证类声明
        assertTrue(uml.contains("class GenericContainer<K, V>"));
        assertTrue(uml.contains("class Pair<T, U>"));

        // 验证字段声明
        assertTrue(uml.contains("- baseMap: Map<K, V>"));
        assertTrue(uml.contains("- first: T"));
        assertTrue(uml.contains("- second: U"));

        // 验证泛型方法签名
        assertTrue(uml.contains("+ <E extends Number> addNumbericEntry(key: K, number: E): void"));
        assertTrue(uml.contains("+ mergeEntries(inputMap: Map<? extends K, ? super V>): void"));
        assertTrue(uml.contains("+ <T, U> {static} zipLists(list1: List<T>, list2: List<U>): List<Pair<T, U>>"));

        // 验证泛型约束方法
        assertTrue(uml.contains("+ <V extends Comparable<V>> getMax(a: V, b: V): V"));

    }

    @Test
    public void testOrderProcessorStructure() throws Exception {
        String uml = generateUML("OrderProcessor.java");

        // 验证类声明和继承关系
        assertTrue(uml.contains("abstract class OrderProcessor<T extends Number>"));
        assertTrue(uml.contains("class DigitalOrderProcessor"));
        assertTrue(uml.contains("OrderProcessor <|-- DigitalOrderProcessor"));

        // 验证字段声明
        assertTrue(uml.contains("# currentStatus: OrderStatus"));
        assertTrue(uml.contains("- description: String"));

        // 验证方法签名
        assertTrue(uml.contains("# <E extends Exception> logError(exception: E): void"));
        assertTrue(uml.contains("+ {abstract} validateOrder(orderId: T): void"));
        assertTrue(uml.contains("+ updateStatus(newStatus: OrderStatus): void"));
        assertTrue(uml.contains("+ <V extends CharSequence> formatReceipt(receiptTemplate: V): String"));

        // 验证枚举结构
        assertTrue(uml.contains("enum OrderStatus"));
        assertTrue(uml.contains("PENDING"));
        assertTrue(uml.contains("PROCESSING"));
        assertTrue(uml.contains("COMPLETED"));
        assertTrue(uml.contains("+ getStatusInfo(): String"));

        // 验证泛型约束
        assertTrue(uml.contains("T extends Number")); // 类级泛型约束
        assertTrue(uml.contains("E extends Exception")); // 方法级泛型约束
        assertTrue(uml.contains("V extends CharSequence")); // 方法级泛型约束

        // 验证方法覆盖
        assertTrue(uml.contains("validateOrder(orderId: Long): void")); // 具体实现方法参数类型
    }
}
