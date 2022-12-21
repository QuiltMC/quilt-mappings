package quilt.internal.input;

public class GClass {
    public static final int TYPE_ONE = 0x1101;
    public static final int TYPE_TWO = 0x1102;
    public static final int TYPE_THREE = 0x1103;
    public static final int TYPE_FOUR = 0x1104;

    public static String fromType(int type) {
        return switch (type) {
            case TYPE_ONE -> "one";
            case TYPE_TWO -> "two";
            case TYPE_THREE -> "three";
            case TYPE_FOUR -> "four";
            default -> null;
        };
    }

    public static int toType(String s) {
        return switch (s) {
            case "one" -> TYPE_ONE;
            case "two" -> TYPE_TWO;
            case "three" -> TYPE_THREE;
            case "four" -> TYPE_FOUR;
            default -> 0;
        };
    }
}
