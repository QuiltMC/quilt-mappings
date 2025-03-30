package quilt.internal.input;

public class EClass {
    public static final int FLAG_A = 0x1;
    public static final int FLAG_B = 0x2;
    public static final int FLAG_C = 0x4;
    public static final int FLAG_D = 0x8;

    public static void m1(int flags) {
        boolean a = (flags & FLAG_A) != 0;
        boolean b = (flags & FLAG_B) != 0;
        boolean c = (flags & FLAG_C) != 0;
        boolean d = (flags & FLAG_D) != 0;
        System.out.println("A " + a + " B " + b + " C " + c + " D " + d);
    }

    public static void m2() {
        m1(FLAG_A | FLAG_C);
    }

    public static void m3() {
        m1(FLAG_A | FLAG_D | FLAG_B);
    }

    public static void m4() {
        m1(FLAG_B);
    }
}
