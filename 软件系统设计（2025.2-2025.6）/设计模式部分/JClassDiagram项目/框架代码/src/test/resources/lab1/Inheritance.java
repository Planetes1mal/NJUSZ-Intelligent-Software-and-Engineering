public class Animal {
    public String name;
    public void eat() {
        System.out.println("Animal is eating");
    }
}

public class Dog extends Animal {
    public String gender;
    public void bark() {
        System.out.println("Dog is barking");
    }

    @Override
    public void eat() {
        System.out.println("Dog is eating");
    }
}

public class Cat extends Animal {
    public String gender;
    public String color;
    public void meow() {
        System.out.println("Cat is meowing");
    }

    @Override
    public void eat() {
        System.out.println("Cat is eating");
    }
}

public class SpecialDog extends Dog {
    public void specialBark() {
        System.out.println("Special dog is barking");
    }
}


