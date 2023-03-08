package quilt.internal.input;

public class PClass {
    public interface AInterface {
        void doFoo();
    }

    public enum BEnum {
        ABC,
        DEF,
        GHI
    }

    public record CRecord(int x, int y, AEnum e) {
        public enum AEnum {
            ZP1,
            Z0,
            ZN1
        }

        public interface BInterface {
            int bar();
        }
    }
}
