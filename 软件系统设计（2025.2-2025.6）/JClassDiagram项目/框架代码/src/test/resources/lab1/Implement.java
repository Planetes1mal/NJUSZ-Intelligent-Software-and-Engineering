interface Instruction {
    void execute();
}

public class BranchInstruction implements Instruction {
    private int rs1;
    private int rs2;
    private int imm;
    private int funct3;

    public void execute() {
        System.out.println("BranchInstruction is executing");
    }

    private int signExtend(int value, int bits) {
        int shift = 32 - bits;
        return (value << shift) >> shift;
    }
}

class Move implements Instruction {
    public void execute() {
        System.out.println("Move is executing");
    }
}



