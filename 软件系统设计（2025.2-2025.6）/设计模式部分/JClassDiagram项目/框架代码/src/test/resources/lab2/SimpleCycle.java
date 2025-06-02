package lab2;

class X {
    private Y y;

    public Y getY() {
        return y;
    }

    public void clear() {
    }
}

class Y {
    private Z z;

    public Z getZ() {
        return z;
    }

    public void clear() {
    }
}

class Z {
    private X x;

    public X getX() {
        return x;
    }

    public void clear() {
    }
}

class A {
    private X x;

    public X getX() {
        return x;
    }

    public void clear() {
    }
}