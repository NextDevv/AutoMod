package it.unilix.automod.utils;

public record Pair<A, B>(A a, B b) {
    public static <A, B> Pair<A, B> of(A a, B b) {
        return new Pair<>(a, b);
    }

    public A getFirst() {
        return a;
    }

    public B getSecond() {
        return b;
    }
}
