import java.util.List;

public class Dice {
    private int it;
    private List<Integer> numberList;

    public Dice(List<Integer> numbers) {
        this.it = 0;
        this.numberList = numbers;
    }

    public int getSteps() {
        int num = numberList.get(it);
        it = (it + 1) % numberList.size();
        return num;
    }
}