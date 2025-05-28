package lab2;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Car {
    private int speed;

    public Car(int speed) {
        this.speed = speed;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}

public class Menu {
    private List<String> items;

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public void addItem(String item) {
        items.add(item);
    }
}

public class Data {
    private HashMap<String, String> map;

    private int size;

    private int flag;

    private boolean dirty;

    public HashMap<String, String> getMap() {
        return map;
    }

    public void setMap(HashMap<String, String> map) {
        this.map = map;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    private Set<String> getKey() {
        return map.keySet();
    }

    private List<String> getValues() {
        return map.values();
    }
}
