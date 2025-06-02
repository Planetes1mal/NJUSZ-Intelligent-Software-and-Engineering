class Engine {
    private String brand;
}

class Wheel {
    private String brand;
    public int speed, size;
}

public interface RunnableCar {

}

public class Car implements RunnableCar {
    public Engine engine;
    public List<Wheel> wheels;
    public Engine backup;
    // 定义一个run方法
    void run() {

    }
}
