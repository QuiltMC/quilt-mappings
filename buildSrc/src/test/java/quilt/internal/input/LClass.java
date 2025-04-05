package quilt.internal.input;

public class LClass {
    public void foo() {
        Object o = new Object() {
            @Override
            public String toString() {
                return "o";
            }

            @Override
            public int hashCode() {
                return -1;
            }
        };

        System.out.println(o);
    }

    public class AInner {
        @Override
        public int hashCode() {
            return 31;
        }
    }

    public class BInner {
        public void bar() {
        }
    }
}
