package quilt.internal.input;

public class KClass {
    public static void main(String[] args) {
        Object o1 = new Object() {
            private String m1() {
                return "o1";
            }

            @Override
            public String toString() {
                return this.m1();
            }
        };
        Object o2 = new Object() {
            @Override
            public String toString() {
                return "o2";
            }
        };
        System.out.println(o1);
        System.out.println(o2);
    }
}
