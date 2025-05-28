public class Bird extends Animal implements Flyable {
    public Bird(String name) {
        super(name);
    }
    
    public String color;
    
    public void fly() {
        System.out.println("fly");
    }
}