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
import command.CommandLineTool;

public class MixTest {
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
    public void testMonopoly() throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource("monopoly");
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        String uml = diagram.generateUML();

        assertTrue(uml.startsWith("@startuml"), "UML diagram should start with @startuml");
        assertTrue(uml.contains("@enduml"), "UML diagram should end with @enduml");

        assertTrue(uml.contains("enum PlayerState {"), "UML diagram should contain enum PlayerState");
        assertTrue(uml.contains("TO_WAITING"), "enum PlayerState should contain TO_WAITING");

        String expecttHouseCell = """
                class HouseCell {
                    + meet(player: Player): void
                }
                """;
        assertTrue(uml.contains(expecttHouseCell), "UML diagram should contain class HouseCell: \n" + expecttHouseCell);

        String expecttMoneyCell = """
                class MoneyCell {
                    - gainMoney: int
                    + meet(player: Player): void
                }
                """;
        assertTrue(uml.contains(expecttMoneyCell), "UML diagram should contain class MoneyCell: \n" + expecttMoneyCell);

        String expecttWaitCell = """
                class WaitCell {
                    + meet(player: Player): void
                }
                """;
        assertTrue(uml.contains(expecttWaitCell), "UML diagram should contain class WaitCell: \n" + expecttWaitCell);

        String expecttDice1 = """
                class Dice {
                    - it: int
                    - numberList: List<Integer>
                    + getSteps(): int
                }
                """;
        String expecttDice2 = """
                class Dice {
                    - numberList: List<Integer>
                    - it: int
                    + getSteps(): int
                }
                """;
        assertTrue(uml.contains(expecttDice1) || uml.contains(expecttDice2),
                "UML diagram should contain class Dice like: \n" + expecttDice1);

        String expecttCell1 = """
                abstract class Cell {
                    # game: Game
                    + setGame(game: Game): void
                    + {abstract} meet(player: Player): void
                }
                """;
        String expecttCell2 = """
                abstract class Cell {
                    # game: Game
                    + {abstract} meet(player: Player): void
                    + setGame(game: Game): void
                }
                """;
        assertTrue(uml.contains(expecttCell1) || uml.contains(expecttCell2),
                "UML diagram should contain class Cell like: \n" + expecttCell1);

        String expecttCellFactory = """
                class CellFactory {
                    + {static} createCell(cellStr: String): Cell
                }
                """;
        assertTrue(uml.contains(expecttCellFactory),
                "UML diagram should contain class CellFactory: \n" + expecttCellFactory);

        assertEquals(1, countSubstring(uml, "Cell <|-- BlankCell"),
                "Cell <|-- BlankCell should appear once");

        assertEquals(1, countSubstring(uml, "Cell <|-- HouseCell"),
                "Cell <|-- HouseCell should appear once");

        assertEquals(1, countSubstring(uml, "Cell <|-- MoneyCell"),
                "Cell <|-- MoneyCell should appear once");

        // 测试关系
        assertEquals(1, countSubstring(uml, "Cell <|-- BlankCell"), "Cell <|-- BlankCell should appear once");
        assertEquals(1, countSubstring(uml, "Player <.. BlankCell"), "Player <.. BlankCell should appear once");
        assertEquals(1, countSubstring(uml, "Game <-- Cell"), "Game <-- Cell should appear once");
        assertEquals(1, countSubstring(uml, "Player <.. Cell"), "Player <.. Cell should appear once");
        assertEquals(1, countSubstring(uml, "Cell <.. CellFactory"), "Cell <.. CellFactory should appear once");
        assertEquals(1, countSubstring(uml, "Dice <-- Game"), "Dice <-- Game should appear once");
        assertEquals(1, countSubstring(uml, "Player <-- Game"), "Player <-- Game should appear once");
        assertEquals(1, countSubstring(uml, "Cell <-- Game"), "Cell <-- Game should appear once");
        assertEquals(1, countSubstring(uml, "PropertyCell <.. Game"), "PropertyCell <.. Game should appear once");
        assertEquals(1, countSubstring(uml, "Cell <|-- HouseCell"), "Cell <|-- HouseCell should appear once");
        assertEquals(1, countSubstring(uml, "Player <.. HouseCell"), "Player <.. HouseCell should appear once");
        assertEquals(1, countSubstring(uml, "Player <.. Main"), "Player <.. Main should appear once");
        assertEquals(1, countSubstring(uml, "Cell <.. Main"), "Cell <.. Main should appear once");
        assertEquals(1, countSubstring(uml, "Game <.. Main"), "Game <.. Main should appear once");
        assertEquals(1, countSubstring(uml, "Cell <|-- MoneyCell"), "Cell <|-- MoneyCell should appear once");
        assertEquals(1, countSubstring(uml, "Player <.. MoneyCell"), "Player <.. MoneyCell should appear once");
        assertEquals(1, countSubstring(uml, "PlayerState <-- Player"), "PlayerState <-- Player should appear once");
        assertEquals(1, countSubstring(uml, "PropertyCell <-- Player"), "PropertyCell <-- Player should appear once");
        assertEquals(1, countSubstring(uml, "Cell <|-- PropertyCell"), "Cell <|-- PropertyCell should appear once");
        assertEquals(1, countSubstring(uml, "Player <-- PropertyCell"), "Player <-- PropertyCell should appear once");
        assertEquals(1, countSubstring(uml, "Cell <|-- RobCell"), "Cell <|-- RobCell should appear once");
        assertEquals(1, countSubstring(uml, "Player <.. RobCell"), "Player <.. RobCell should appear once");
        assertEquals(1, countSubstring(uml, "Cell <|-- WaitCell"), "Cell <|-- WaitCell should appear once");
        assertEquals(1, countSubstring(uml, "Player <.. WaitCell"), "Player <.. WaitCell should appear once");

        List<String> codeSmells = diagram.getCodeSmells();

        assertTrue(codeSmells.contains("Lazy Class: HouseCell"), "HouseCell should be a lazy class");
        assertTrue(codeSmells.contains("Lazy Class: MoneyCell"), "MoneyCell should be a lazy class");
        assertTrue(codeSmells.contains("Lazy Class: WaitCell"), "WaitCell should be a lazy class");
        assertTrue(codeSmells.contains("Lazy Class: Dice"), "Dice should be a lazy class");
        assertTrue(codeSmells.contains("Lazy Class: RobCell"), "RobCell should be a lazy class");
        assertTrue(codeSmells.contains("Lazy Class: CellFactory"), "CellFactory should be a lazy class");
        assertTrue(codeSmells.contains("Lazy Class: Main"), "Main should be a lazy class");
        assertTrue(codeSmells.contains("Lazy Class: BlankCell"), "BlankCell should be a lazy class");

        // 测试循环依赖
        assertTrue(containsCycle(codeSmells, "Player <.. Game <.. Cell <.. PropertyCell <.. Player")||
                containsCycle(codeSmells, "Game <.. Cell <.. Game")||
                containsCycle(codeSmells, "Game <.. Cell <.. PropertyCell <.. Game")||
                containsCycle(codeSmells, "PropertyCell <.. Player <.. PropertyCell")||
                containsCycle(codeSmells, "Cell <.. PropertyCell <.. Player <.. Cell"),
                "you should report at least one correct circular dependency");

        // 测试设计模式
        assertTrue(!codeSmells.contains("Possible Design Patterns: Strategy Pattern"),
                "Strategy Pattern should not be detected");
        assertTrue(!codeSmells.contains("Possible Design Patterns: Singleton Pattern"),
                "Singleton Pattern should not be detected");

        // 测试命令行工具
        CommandLineTool commandLine = new CommandLineTool(diagram);

        String result = commandLine.execute("query -c Main");
        assertEquals("""
                class Main {
                    + {static} main(args: String[]): void
                }
                """, result, "query -c Main Error");

        // modify field <target-name> -n <field-name> [--new-name=<name>]
        // [--new-type=<type>] [--new-access=<modifer>] [--static]
        commandLine.execute("modify field Dice -n it --new-type=Integer --new-access=# --static");
        result = commandLine.execute("query -c Dice --hide=method");
        assertEquals("""
                class Dice {
                    - numberList: List<Integer>
                    # {static} it: Integer
                }
                """, result,
                "modify field Dice -n it --new-type=Integer --new-access=# --static Error Or query -c Dice --hide=method Error");

        // undo
        commandLine.execute("undo");
        result = commandLine.execute("query -c Dice --hide=method");

        assertTrue(result.equals("""
                class Dice {
                    - numberList: List<Integer>
                    - it: int
                }
                """) || result.equals("""
                class Dice {
                    - it: int
                    - numberList: List<Integer>
                }
                """), "undo Error Or query -c Dice --hide=method Error");

        // add -e <name> [--values=<constant list>]
        commandLine.execute("add -e GameState --values=WAITING,PLAYING,END");
        result = commandLine.execute("query -e GameState");

        assertTrue(result.contains("enum GameState {") &&
                result.contains("WAITING") &&
                result.contains("PLAYING") &&
                result.contains("END"),
                "add -e GameState --values=WAITING,PLAYING,END Error Or query -c GameState Error");

        // 测试级联删除
        commandLine.execute("delete -c WaitCell");

        uml = diagram.generateUML();

        assertEquals(0, countSubstring(uml, "Cell <|-- WaitCell"), "Cell <|-- WaitCell should not appear");
        assertEquals(0, countSubstring(uml, "Player <.. WaitCell"), "Player <.. WaitCell should not appear");

        commandLine.execute("undo");

        uml = diagram.generateUML();

        assertEquals(1, countSubstring(uml, "Cell <|-- WaitCell"), "Cell <|-- WaitCell should appear");
        assertEquals(1, countSubstring(uml, "Player <.. WaitCell"), "Player <.. WaitCell should appear");
    }

    @Test
    public void testLambda() throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getResource("lambda");
        Path resourcePath = null;
        if (resourceUrl != null) {
            resourcePath = Paths.get(resourceUrl.toURI());
        }
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(resourcePath);
        String uml = diagram.generateUML();

        assertTrue(uml.startsWith("@startuml"), "UML diagram should start with @startuml");
        assertTrue(uml.contains("@enduml"), "UML diagram should end with @enduml");

        assertTrue(uml.contains("enum TokenType {"), "UML diagram should contain enum TokenType");
        assertTrue(uml.contains("+ tokenize(): List<Token>"), "UML diagram should contain + tokenize(): List<Token>");

        String expectVarExpr = """
                class VarExpr {
                    + name: String
                    + <T> accept(visitor: Visitor<T>): T
                }
                """;
        assertTrue(uml.contains(expectVarExpr), "UML diagram should contain class VarExpr: \n" + expectVarExpr);

        String expectExpr = """
                interface Expr {
                    + <T> accept(visitor: Visitor<T>): T
                }
                """;
        assertTrue(uml.contains(expectExpr), "UML diagram should contain interface Expr: \n" + expectExpr);

        assertEquals(1, countSubstring(uml, "Expr <|.. AppExpr"), "Expr <|.. AppExpr should appear once");
        assertEquals(1, countSubstring(uml, "Expr <-- AppExpr"), "Expr <-- AppExpr should appear once");
        assertEquals(1, countSubstring(uml, "Visitor <.. AppExpr"), "Visitor <.. AppExpr should appear once");
        assertEquals(1, countSubstring(uml, "Visitor <.. Expr"), "Visitor <.. Expr should appear once");
        assertEquals(1, countSubstring(uml, "Expr <|.. LambdaExpr"), "Expr <|.. LambdaExpr should appear once");
        assertEquals(1, countSubstring(uml, "Expr <-- LambdaExpr"), "Expr <-- LambdaExpr should appear once");
        assertEquals(1, countSubstring(uml, "Visitor <.. LambdaExpr"), "Visitor <.. LambdaExpr should appear once");
        assertEquals(1, countSubstring(uml, "Visitor <.. Expr"), "Visitor <.. Expr should appear once");

        assertEquals(1, countSubstring(uml, "Expr <|.. VarExpr"), "Expr <|.. VarExpr should appear once");
        assertEquals(1, countSubstring(uml, "Visitor <.. VarExpr"), "Visitor <.. VarExpr should appear once");

        assertEquals(1, countSubstring(uml, "Token <.. Lexer"), "Token <.. Lexer should appear once");
        assertEquals(1, countSubstring(uml, "TokenType <-- Token"), "TokenType <-- Token should appear once");
        assertEquals(1, countSubstring(uml, "Token <-- Parser"), "Token <-- Parser should appear once");

        assertEquals(2, countSubstring(uml, "Expr <.. Parser"),
                "Expr <.. Parser and LambdaExpr <.. Parser should appear");
        assertEquals(1, countSubstring(uml, "LambdaExpr <.. Parser"), "LambdaExpr <.. Parser should appear once");
        assertEquals(1, countSubstring(uml, "TokenType <.. Parser"), "TokenType <.. Parser should appear once");

        assertEquals(1, countSubstring(uml, "LambdaExpr <.. Visitor"), "LambdaExpr <.. Visitor should appear once");
        assertEquals(1, countSubstring(uml, "VarExpr <.. Visitor"), "VarExpr <.. Visitor should appear once");
        assertEquals(1, countSubstring(uml, "AppExpr <.. Visitor"), "AppExpr <.. Visitor should appear once");

        // 加载配置文件
        URL configUrl = getClass().getResource("config_lambda.xml");
        Path configPath = null;
        if (configUrl != null) {
            configPath = Paths.get(configUrl.toURI());
        } else {
            throw new IOException("Config file not found");
        }
        diagram.loadConfig(configPath.toString());

        List<String> codeSmells = diagram.getCodeSmells();

        assertTrue(!codeSmells.contains("Lazy Class: LambdaExpr"), "Class Analyzer should not work");
        assertTrue(!codeSmells.contains("Lazy Class: AppExpr"), "Class Analyzer should not work");
        assertTrue(!codeSmells.contains("Lazy Class: VarExpr"), "Class Analyzer should not work");
        assertTrue(!codeSmells.contains("Lazy Class: Token"), "Class Analyzer should not work");

        assertTrue(containsCycle(codeSmells, "Expr <.. LambdaExpr <.. Visitor <.. Expr")||
                        containsCycle(codeSmells, "LambdaExpr <.. Visitor <.. LambdaExpr")||
                        containsCycle(codeSmells, "VarExpr <.. Visitor <.. VarExpr")||
                        containsCycle(codeSmells, "AppExpr <.. Visitor <.. AppExpr")||
                        containsCycle(codeSmells, "Expr <.. VarExpr <.. Visitor <.. Expr")||
                        containsCycle(codeSmells, "Expr <.. AppExpr <.. Visitor <.. Expr"),
                "Circular Dependency Analyzer should work and you should report at least one circular dependency");

        CommandLineTool commandLine = new CommandLineTool(diagram);

        String result = commandLine.execute("query -i Visitor");
        assertTrue(result.contains("interface Visitor {") || result.contains("interface Visitor<T> {"),
                "query -i Visitor Error, you should report: interface Visitor {, but your result is:\n" + result);
        assertTrue(result.contains("+ visitLambda(expr: LambdaExpr): T"),
                "query -i Visitor Error, you should report: + visitLambda(expr: LambdaExpr): T, but your result is:\n"
                        + result);
        assertTrue(result.contains("+ visitVar(expr: VarExpr): T"),
                "query -i Visitor Error, you should report: + visitVar(expr: VarExpr): T, but your result is:\n"
                        + result);
        assertTrue(result.contains("+ visitApp(expr: AppExpr): T"),
                "query -i Visitor Error, you should report: + visitApp(expr: AppExpr): T, but your result is:\n"
                        + result);

        result = commandLine.execute("relate Expr AppExpr");
        assertTrue(
                result.contains("Expr <|.. AppExpr") 
                        && result.contains("Expr <-- AppExpr"),
                "relate Expr AppExpr Error, you should report: Expr <|.. AppExpr, Expr <-- AppExpr but your result is:\n"
                        + result);
        
        commandLine.execute("delete -c VarExpr");
        uml = diagram.generateUML();
        assertEquals(0, countSubstring(uml, "Expr <|.. VarExpr"), "VarExpr should be deleted");

        commandLine.execute("undo");
        uml = diagram.generateUML();
        assertEquals(1, countSubstring(uml, "Expr <|.. VarExpr"), "VarExpr should be restored");

        String res = commandLine.execute("undo");
        assertTrue(res.contains("No command to undo"), "No more undo Error");

        commandLine.execute("add function Token -n toString -t String --access=+");
        commandLine.execute("modify function Lexer -n tokenize --new-params=config:String");

        res = commandLine.execute("query -c Token");
        assertTrue(res.contains("+ toString(): String"), "toString method should be added");

        res = commandLine.execute("query -c Lexer");
        assertTrue(res.contains("+ tokenize(config: String): List<Token>"), "tokenize method should be modified");

        commandLine.execute("undo");
        res = commandLine.execute("query -c Lexer");
        assertTrue(res.contains("+ tokenize(): List<Token>"), "tokenize method should be restored");
        res = commandLine.execute("query -c Token");
        assertTrue(res.contains("+ toString(): String"), "toString method should be added");
    }

}
