package quilt.internal.input;

public class OClass implements MInterface, NInterface {
    @Override
    public void otherMMethod() {
        System.out.println("Foo");
    }

    @Override
    public void convergingMethod() {
        System.out.println("Implementing HInterface and NInterface");
    }

    @Override
    public int nMethod(boolean b) {
        return (b ? 1 : -1) << 31;
    }

    @Override
    public void otherNMethod() {
        System.out.println("Bar");
    }

    @Override
    public void mMethod(String s) {
        System.out.println(s.hashCode());
    }
}
