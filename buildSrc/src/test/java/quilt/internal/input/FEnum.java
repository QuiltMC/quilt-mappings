package quilt.internal.input;

public enum FEnum {
    ALL(0x1 | 0x2),
    NONE(0),
    LEFT(0x1),
    RIGHT(0x2);

    public static final int FLAG_LEFT = 0x1;
    public static final int FLAG_RIGHT = 0x2;

    private final int flags;

    FEnum(int flags) {
        this.flags = flags;
    }

    public boolean hasLeft() {
        return (flags & FLAG_LEFT) != 0;
    }

    public boolean hasRight() {
        return (flags & FLAG_RIGHT) != 0;
    }
}
