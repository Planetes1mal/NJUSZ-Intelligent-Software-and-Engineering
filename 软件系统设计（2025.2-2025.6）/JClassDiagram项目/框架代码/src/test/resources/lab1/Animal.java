public class Animal {
    private String name;
    public static int count;

    public Animal(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

public class Dog extends Animal {
    public Dog(String name) {
        super(name);
    }
    
        public String gender;

    public void wolf() {
        System.out.println("wolf");
    }
}

public interface Flyable {
    public void fly();
}

public class Bird extends Animal implements Flyable {
    public Bird(String name) {
        super(name);
    }
    
    public String color;
    
    public void fly() {
        System.out.println("fly");
    }
}