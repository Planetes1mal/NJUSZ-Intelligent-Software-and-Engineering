package lab3;

public class RV32Machine2 {
    public Memory memory;
    private CacheStrategy cacheStrategy;
}

public class Memory {
    private Byte[] memory;

    public int size;

    private static Memory instance;

    public Memory(int size) {
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
    void schedule();
}

public class FIFOStrategy implements CacheStrategy {
    public void schedule() {
        System.out.println("FIFO");
    }
}

public class LRUStrategy implements CacheStrategy {
    public void schedule() {
        System.out.println("LRU");
    }
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

