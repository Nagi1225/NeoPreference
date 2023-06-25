package com.nagi.neopreference;

import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    private T value;
    private volatile boolean isEvaluated = false;

    public static <T> Lazy<T> from(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    public static <T> Lazy<T> lazy(Supplier<T> supplier) {
        return from(supplier);
    }

    private Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (!isEvaluated) {
            synchronized (this) {
                if (!isEvaluated) {
                    value = supplier.get();
                    isEvaluated = true;
                }
            }
        }
        return value;
    }
}
