import java.io.IOException;
import java.nio.file.Paths;

import command.CommandLineTool;
import diagram.ClassDiagram;
import diagram.ClassDiagramGenerator;

public class JClassDiagram {

    public static void main(String[] args) throws IOException {
        ClassDiagramGenerator generator = new ClassDiagramGenerator();
        ClassDiagram diagram = generator.parse(Paths.get(args[0]));
        CommandLineTool commandLineTool = new CommandLineTool(diagram);

        while (true) {
            System.out.print("> ");
            String command = System.console().readLine();
            if (command.equals("exit")) {
                break;
            }
            if (command.equals("diagram")) {
                System.out.println(diagram.generateUML());
                continue;
            }
            String result = commandLineTool.execute(command);
            System.out.println(result);
        }
    }
}
