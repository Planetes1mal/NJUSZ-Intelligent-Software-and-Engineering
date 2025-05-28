package lab2;

public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }
}

public class OnlineJudger2 {
    public String judge(int id, List<Paper> papers) {
        Calculator calc = new Calculator();
        int ans = calc.add(1, 2);
        // do something
    }

    protected List<Paper> getPapers() {
        // do something
    }
}

public class PaperResult {
    public String res;
}

public class Paper {
    String paper;
}
