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

public class RegPool {
    private static RegPool instance;

    private RegPool() {
    }

    public static RegPool getInstance() {
        if (instance == null) {
            instance = new RegPool();
        }
        return instance;
    }
}