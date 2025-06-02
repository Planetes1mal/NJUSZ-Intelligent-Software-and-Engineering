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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
                        index += sub.length(); // 移动到下一个位置继续查找
                }
                return count;
        }

        public void testExsitUML(String uml, String umlStr) {
                assertTrue(uml.contains(umlStr), "Missing " + umlStr);
        }

        public void testNotExsitUML(String uml, String umlStr) {
                assertFalse(uml.contains(umlStr), umlStr + "should not exist.");
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
        public void testVehicleSystemRelationships() throws URISyntaxException, IOException {
                // 生成UML图内容
                String uml = generateUML("Vehicle.java");

                // 验证基础结构
                assertTrue(uml.startsWith("@startuml"), "Missing PlantUML start tag");
                assertTrue(uml.contains("@enduml"), "Missing PlantUML end tag");

                // 验证枚举定义
                String expectedEnum1 = "enum VehicleState {\n    RUNNING\n    STOPPED\n    MAINTENANCE\n}";
                String expectedEnum2 = "enum VehicleState {\n    RUNNING\n    MAINTENANCE\n    STOPPED\n}";
                String expectedEnum3 = "enum VehicleState {\n    STOPPED\n    RUNNING\n    MAINTENANCE\n}";
                String expectedEnum4 = "enum VehicleState {\n    STOPPED\n    MAINTENANCE\n    RUNNING\n}";
                String expectedEnum5 = "enum VehicleState {\n    MAINTENANCE\n    STOPPED\n    RUNNING\n}";
                String expectedEnum6 = "enum VehicleState {\n    MAINTENANCE\n    RUNNING\n    STOPPED\n}";
                assertTrue(uml.contains(expectedEnum1) ||
                        uml.contains(expectedEnum2) ||
                        uml.contains(expectedEnum3) ||
                        uml.contains(expectedEnum4) ||
                        uml.contains(expectedEnum5) ||
                        uml.contains(expectedEnum6), "VehicleState enum values incorrect");
                assertEquals(1, countSubstring(uml, "enum VehicleState {"),
                        "Multiple enum VehicleState");

                // 验证SportsCar类结构
                String expectedSportsCar = "class SportsCar {\n"
                        + "    - horsepower: int\n"
                        + "    + start(): VehicleState\n"
                        + "}";
                assertTrue(uml.contains(expectedSportsCar), "SportsCar attributes/methods mismatch");

                // 验证Garage类复杂泛型
                String expectedGarage1 = "class Garage {\n"
                        + "    - vehicleMap: Map<String, Vehicle>\n"
                        + "    - maintenanceService: VehicleService<Vehicle>\n"
                        + "    + addVehicle(key: String, vehicle: Vehicle): void\n"
                        + "    + getVehiclesByState(): Map<VehicleState, List<Vehicle>>\n"
                        + "}";
                String expectedGarage2 = "class Garage {\n"
                        + "    - maintenanceService: VehicleService<Vehicle>\n"
                        + "    - vehicleMap: Map<String, Vehicle>\n"
                        + "    + addVehicle(key: String, vehicle: Vehicle): void\n"
                        + "    + getVehiclesByState(): Map<VehicleState, List<Vehicle>>\n"
                        + "}";
                String expectedGarage3 = "class Garage {\n"
                        + "    - vehicleMap: Map<String, Vehicle>\n"
                        + "    - maintenanceService: VehicleService<Vehicle>\n"
                        + "    + getVehiclesByState(): Map<VehicleState, List<Vehicle>>\n"
                        + "    + addVehicle(key: String, vehicle: Vehicle): void\n"
                        + "}";
                String expectedGarage4 = "class Garage {\n"
                        + "    - maintenanceService: VehicleService<Vehicle>\n"
                        + "    - vehicleMap: Map<String, Vehicle>\n"
                        + "    + getVehiclesByState(): Map<VehicleState, List<Vehicle>>\n"
                        + "    + addVehicle(key: String, vehicle: Vehicle): void\n"
                        + "}";
                assertTrue(uml.contains(expectedGarage1) ||
                        uml.contains(expectedGarage2) ||
                        uml.contains(expectedGarage3) ||
                        uml.contains(expectedGarage4), "Garage generic parameters error");

                // 验证抽象类结构
                String expectedAbstractVehicle1 = "abstract class AbstractVehicle {\n"
                        + "    # licensePlate: String\n"
                        + "    # state: VehicleState\n"
                        + "    + getLicensePlate(): String\n"
                        + "}";

                String expectedAbstractVehicle2 = "abstract class AbstractVehicle {\n"
                        + "    # state: VehicleState\n"
                        + "    # licensePlate: String\n"
                        + "    + getLicensePlate(): String\n"
                        + "}";
                assertTrue(uml.contains(expectedAbstractVehicle1) || uml.contains(expectedAbstractVehicle2),
                        "AbstractVehicle abstract markers missing");

                // 验证接口方法签名
                String expectedVehicleInterface1 = "interface Vehicle {\n"
                        + "    + start(): VehicleState\n"
                        + "    + stop(): VehicleState\n"
                        + "}";

                String expectedVehicleInterface2 = "interface Vehicle {\n"
                        + "    + stop(): VehicleState\n"
                        + "    + start(): VehicleState\n"
                        + "}";
                assertTrue(uml.contains(expectedVehicleInterface1) || uml.contains(expectedVehicleInterface2),
                        "Vehicle interface methods incorrect");

                // 验证泛型服务类
                assertTrue(uml.contains("class VehicleService<T extends Vehicle> {\n"
                        + "    + groupByState(vehicles: List<T>): Map<VehicleState, List<T>>\n"
                        + "}"), "Generic service class error");

                // 验证Main类静态方法
                assertTrue(uml.contains("class Main {\n"
                        + "    + {static} main(args: String[]): void\n"
                        + "}"), "Static main method format error");

                // 验证继承链
                assertTrue(uml.contains("AbstractVehicle <|-- Car"),
                        "Car should extend AbstractVehicle");
                assertTrue(uml.contains("Car <|-- SportsCar"),
                        "SportsCar should extend Car");

                // 验证接口实现
                assertTrue(uml.contains("Vehicle <|.. AbstractVehicle"),
                        "AbstractVehicle should implement Vehicle");

                // 验证状态依赖关系
                assertTrue(uml.contains("VehicleState <.. Garage"),
                        "Garage should depend on VehicleState");
                assertTrue(uml.contains("VehicleState <.. VehicleService"),
                        "Service should depend on State");

                // 验证主类依赖
                assertTrue(uml.contains("VehicleService <.. Main"),
                        "Main should use VehicleService");
                assertTrue(uml.contains("Garage <.. Main"),
                        "Main should interact with Garage");
                assertFalse(uml.contains("String <-- Garage"), "String should not appear in relationship");

                // 验证关联关系
                assertTrue(uml.contains("Vehicle <-- Garage"), "Missing Vehicle <-- Garage");
                assertTrue(uml.contains("VehicleService <-- Garage"), "Missing VehicleService <-- Garage");
                assertTrue(uml.contains("VehicleState <-- AbstractVehicle"),
                        "Missing VehicleState <-- AbstractVehicle");

                // 防御性关系唯一性检查
                assertEquals(1, countSubstring(uml, "Car <|-- SportsCar"),
                        "Multiple car inheritance lines");
                assertEquals(6, countSubstring(uml, "VehicleState <.."),
                        "VehicleState dependency count mismatch");
        }
}
