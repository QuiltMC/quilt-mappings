package quilt.internal.input;

// empty intermediary + non-empty hashed testing
public class Keep {
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
        // This wouldn't have name in intermediary
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
