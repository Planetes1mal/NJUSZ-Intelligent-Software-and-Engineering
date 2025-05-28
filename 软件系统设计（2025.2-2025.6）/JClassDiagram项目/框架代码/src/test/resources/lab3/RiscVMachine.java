package lab3;

public class RiscVMachine {
    public Memory memory;
}

public class Memory {
    private Byte[] memory;

    public int size;

    private static Memory instance;

    private Memory(int size) {
        memory = new Byte[size];
        this.size = size;
    }
    
    public static Memory getInstance() {
        if (instance == null) {
            instance = new Memory(1024);
        }
        return instance;
    }
}

public interface CacheStrategy {
}

public class FIFOStrategy implements CacheStrategy {
    
}

public class LRUStrategy implements CacheStrategy {
    
}

public class StrategyFactory {
    public static Strategy getStrategy(String strategy) {
        if (strategy.equals("FIFO")) {
            return new FIFOStrategy();
        } else if (strategy.equals("LRU")) {
            return new LRUStrategy();
        }
        return null;
    }
}

