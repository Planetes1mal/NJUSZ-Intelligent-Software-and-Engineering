package lab2;

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
    public Engine backupEngine;
    void run() {

    }

    Car copy() {
        
    }
}

public class RedCar extends Car {

}

public class BlueCar extends Car {

}

public class ParkingLot {
    private Car[] cars;
    public int size;
    Car checkCar(int index) {
        return cars[index];
    }
}

class RedCarFactory {
    public static RedCar createCar() {
        return new RedCar();
    }
}
