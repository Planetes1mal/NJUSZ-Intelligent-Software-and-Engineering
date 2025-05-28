package lab2;

public class A {
    public B b;

    public B createB() {
        return new B();
    }
}

public class B {
    public A a;
    private int id;

    public int getId() {
        return id;
    }
}

public interface C {
    void doSomething();
}

public class D implements C {
    @Override
    public void doSomething() {
        //  do something 
    }
}

public class E {
    public void hello() {
        C cc = new D();
        cc.doSomething();
    }

    protected void bye() {
        // bye
    }
}

public class F {
    public E getE() {
        return new E();
    }

    private int isBigE(E e) {
        return 0;
    }
}