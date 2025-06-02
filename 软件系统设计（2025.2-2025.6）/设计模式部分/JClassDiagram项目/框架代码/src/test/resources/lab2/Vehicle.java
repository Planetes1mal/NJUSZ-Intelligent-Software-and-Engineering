package lab2;

// 接口定义
interface Vehicle {
    VehicleState start();
    VehicleState stop();
}

// 枚举类
enum VehicleState {
    RUNNING, STOPPED, MAINTENANCE
}

// 抽象类
abstract class AbstractVehicle implements Vehicle {
    protected String licensePlate;
    protected VehicleState state = VehicleState.STOPPED;

    public AbstractVehicle(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getLicensePlate() {
        return licensePlate;
    }
}

// 具体类继承
class Car extends AbstractVehicle {
    public Car(String licensePlate) {
        super(licensePlate);
    }

    @Override
    public VehicleState start() {
        state = VehicleState.RUNNING;
        return state;
    }

    @Override
    public VehicleState stop() {
        state = VehicleState.STOPPED;
        return state;
    }
}

// 继承层次
class SportsCar extends Car {
    private int horsepower;

    public SportsCar(String licensePlate, int horsepower) {
        super(licensePlate);
        this.horsepower = horsepower;
    }

    @Override
    public VehicleState start() {
        state = VehicleState.RUNNING;
        System.out.println("Sports car roaring with " + horsepower + "hp!");
        return state;
    }
}

// 关联关系类
class Garage {
    private final Map<String, Vehicle> vehicleMap = new HashMap<>();
    private final VehicleService<Vehicle> maintenanceService;

    // 依赖注入
    public Garage(VehicleService<Vehicle> maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    public void addVehicle(String key, Vehicle vehicle) {
        vehicleMap.put(key, vehicle);
    }

    // 使用泛型方法
    public Map<VehicleState, List<Vehicle>> getVehiclesByState() {
        return maintenanceService.groupByState(new ArrayList<>(vehicleMap.values()));
    }
}

// 泛型类
class VehicleService<T extends Vehicle> {
    public Map<VehicleState, List<T>> groupByState(List<T> vehicles) {
        Map<VehicleState, List<T>> stateMap = new EnumMap<>(VehicleState.class);
        for (T vehicle : vehicles) {
            stateMap.computeIfAbsent(vehicle.stop(), k -> new ArrayList<>()).add(vehicle);
        }
        return stateMap;
    }
}

// 使用示例
public class Main {
    public static void main(String[] args) {
        VehicleService<Vehicle> service = new VehicleService<>();
        Garage garage = new Garage(service);

        garage.addVehicle("CAR1", new Car("ABC123"));
        garage.addVehicle("SPORT1", new SportsCar("SPRT001", 450));
        
        Map<VehicleState, List<Vehicle>> grouped = garage.getVehiclesByState();
        System.out.println("Stopped vehicles: " + grouped.get(VehicleState.STOPPED).size());
    }
}