package com.nextdevv.automod.utils;

public class Pair<A, B> {
    private A a;
    private B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getFirst() {
        return a;
    }

    public B getSecond() {
        return b;
    }

    public void setFirst(A l) {
        this.a = l;
    }

    public void setSecond(B r) {
        this.b = r;
    }
}
