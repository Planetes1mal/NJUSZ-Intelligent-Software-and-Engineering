public class A {
    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}

public class B {
    private String name;
    public String gender;

    public void doSomething() {
        System.out.println("B is doing something");
    }
}

public class C {
    public void run() {
        System.out.println("C is running");
    }
}