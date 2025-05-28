package lab3;

public class Duck {
    FlyBehavior flyBehavior;
    QuackBehavior quackBehavior;

    public void performFly() {
        flyBehavior.fly();
    }
    
}

public interface QuackBehavior {
    void quack();
}

public class Quack implements QuackBehavior {
    public void quack() {
        System.out.println("Quack");
    }
}

public class Squeak implements QuackBehavior {
    public void quack() {  
        System.out.println("Squeak");
    }
}

public class MuteQuack implements QuackBehavior {
    public void quack() {   
        System.out.println("<< Silence >>");
    }
}

public interface FlyBehavior {
    void fly();
}

public class FlyWithWings implements FlyBehavior {
    public void fly() {
        System.out.println("I'm flying!!");
    }
}

public class FlyNoWay implements FlyBehavior {
    public void fly() {
        System.out.println("I can't fly");
    }
}

public class RedDuck extends Duck {
    Farmer farmer;
    public RedDuck() {
        flyBehavior = new FlyWithWings();
        quackBehavior = new Quack();
    }

    public void display() {
        System.out.println("I'm a real Red Duck");
    }
}

public class BlueDuck extends Duck {
    public BlueDuck() {
        flyBehavior = new FlyWithWings();
        quackBehavior = new Quack();
    }
    
}

public class RubberDuck extends Duck {
    public RubberDuck() {
        flyBehavior = new FlyNoWay();
        quackBehavior = new Squeak();
    }
}

public class GreenDuck extends Duck {
    public GreenDuck() {
        flyBehavior = new FlyWithWings();
        quackBehavior = new Quack();
    }
}

public class GoodDuck extends Duck {
    public GoodDuck() {
        flyBehavior = new FlyWithWings();
        quackBehavior = new Quack();
    }
}

public class BadDuck extends Duck {
    public BadDuck() {
        flyBehavior = new FlyNoWay();
        quackBehavior = new MuteQuack();
    }
}

public class NewDuck extends Duck {
    public NewDuck() {
        flyBehavior = new FlyWithWings();
        quackBehavior = new Quack();
    }
}

public class HappyDuck extends Duck {
    public HappyDuck() {
        flyBehavior = new FlyWithWings();
        quackBehavior = new Quack();
    }
}

public class AngryDuck extends Duck {
    public AngryDuck() {
        flyBehavior = new FlyWithWings();
        quackBehavior = new Quack();
    }
}

public class SadDuck extends Duck {
    public SadDuck() {
        flyBehavior = new FlyWithWings();
        quackBehavior = new Quack();
    }
}

public class BigDuck extends Duck {
    public BigDuck() {
        flyBehavior = new FlyWithWings();
        quackBehavior = new Quack();
    }
}

public class SmallDuck extends Duck {
    public SmallDuck() {
        flyBehavior = new FlyWithWings();
        quackBehavior = new Quack();
    }
}

public class Farmer {
    private String name;
    public String getName() {
        return name;
    }
    public void feed(Duck duck) {
        System.out.println("Feeding the duck");
    }

    public void feed(RedDuck duck) {
        System.out.println("Feeding the duck");
    }

    public static Farmer getInstance() {
        return new Farmer();
    }
}





