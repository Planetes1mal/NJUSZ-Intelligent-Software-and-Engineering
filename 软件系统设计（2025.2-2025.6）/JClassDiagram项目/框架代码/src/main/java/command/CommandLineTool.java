package command;

import diagram.ClassDiagram;

public class CommandLineTool {
    private ClassDiagram diagram;

    public CommandLineTool(ClassDiagram diagram) {
        this.diagram = diagram;
    }

    /**
     * @param command 输入的命令
     * @return 如果是查询性质语句，将查询的结果保存在返回值中。Undo语句可能返回的信息也保存在返回值中。
     */
    public String execute(String command) {
        // TODO: finish me
        return "";
    }
}