package quilt.internal.input;

public record DRecord<L, R>(L left, R right) {
    public static <L, R> DRecord<L, R> of(L left, R right) {
        return new DRecord<>(left, right);
    }
}
