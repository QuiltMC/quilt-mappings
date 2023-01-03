package quilt.internal.input;

public class AClass {
    public int val;
    public float factor;
    public final String name;

    public AClass(int val, int factor, String name) {
        this.val = val;
        this.factor = factor;
        this.name = name;
    }

    public void m1() {
    }

    public int m2() {
        return 76;
    }

    public void m3(int a1) {
    }

    public int m4(int a1, int a2) {
        return 95 + a1 * a2;
    }
}
