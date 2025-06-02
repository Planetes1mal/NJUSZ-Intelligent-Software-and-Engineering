public interface A {
  void doA();  
}

public interface B extends A {
    void doB();
}

public interface C extends A {
    void doC();
}

public interface D extends B, C {
    void doD();
}