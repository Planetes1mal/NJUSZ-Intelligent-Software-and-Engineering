import java.io.IOException;
import java.nio.file.Paths;

import diagram.ClassDiagram;
import diagram.ClassDiagramGenerator;

public class Main {
    public static void main(String[] args) throws IOException {
        // 创建一个ClassDiagramGenerator对象
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        // 解析指定路径下的Animal.java文件，生成ClassDiagram对象
        ClassDiagram diagram = generator.parse(Paths.get("path of java file"));
        // 输出生成的UML图
        System.out.println(diagram.generateUML());
    }
}
