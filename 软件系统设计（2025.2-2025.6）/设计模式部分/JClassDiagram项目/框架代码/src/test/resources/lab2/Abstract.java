// AbstractShape.java
public abstract class AbstractShape {
    protected String color;

    public AbstractShape(String color) {
        this.color = color;
    }

    // 抽象方法
    public abstract double calculateArea();

    // 具体方法
    public String getColor() {
        return color;
    }
}

// Circle.java
public class Circle extends AbstractShape {
    private double radius;

    public Circle(String color, double radius) {
        super(color);
        this.radius = radius;
    }

    @Override
    public double calculateArea() {
        return Math.PI * radius * radius;
    }

    public double getCircumference() {
        return 2 * Math.PI * radius;
    }
}

// ShapeProcessor.java
public abstract class ShapeProcessor {
    public abstract void processShape(AbstractShape shape);

    protected void logProcessing() {
        System.out.println("Processing started");
    }
}