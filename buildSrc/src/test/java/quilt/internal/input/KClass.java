package quilt.internal.input;

// inner named + outer obf testing
public class KClass {
    public static final String f1 = "Hello world";

    public void m1(String s) {
    }

    public int m2() {
        return 1;
    }

    public static class AInner {
        public int m3(String s) {
            return s.hashCode() * 31;
        }

        public class AInnerInner {
            public int m4(String s) {
                return AInner.this.m3(s) << 24 | 0xFF;
            }
        }
    }
}
