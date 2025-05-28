package lab2;

interface A {
}

abstract class B implements A {
    private F f;

    public abstract int add(int x, int y);

    public abstract int sub(int x, int y);
}

class C extends B {
    private static final int E = 1;

    public int add(int a, int b) {
        X x = new X();
        return x.addOne(a) + x.subOne(b);
    }

    public int sub(int a, int b) {
        X x = new X();
        return x.addOne(a) - x.subOne(b);
    }
}

class D {
    private B b;

    public String toString() {
        return "";
    }

    public void empty() {
    }
}

class E {
    private A a;

    public D createD() {
    }

    public x createX() {
    }
}

class F {
    private E e;

    public String toString() {
        return "";
    }

    public void empty(X x) {
    }
}

class X {
    private static final int E = 1;

    public int addOne(int x) {
        return x++;
    }

    public int subOne(int x) {
        return x--;
    }
}
