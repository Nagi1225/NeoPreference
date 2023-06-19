package com.nagi.neopreference;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class Matcher {

    public static <T> ConsumerMatcher<T> matchFor(T t) {
        return new ConsumerMatcherImpl<>(t);
    }

    public static <T> StartFunctionMatcher<T> matchFrom(T t) {
        return new StartFunctionMatcher<T>() {
            @Override
            public <K, V> FunctionMatcher<T, V> typeOf(Class<K> kClass, Function<K, V> function) {
                if (kClass != null && kClass.isInstance(t)) {
                    return new ResultFunctionMatcher<>(function.apply((K) t));
                } else {
                    return new FunctionMatcherImpl<>(t);
                }
            }

            @Override
            public <K, V> FunctionMatcher<T, V> valueOf(K value, Function<K, V> function) {
                if (Objects.equals(value, t)) {
                    return new ResultFunctionMatcher<>(function.apply((K) t));
                } else {
                    return new FunctionMatcherImpl<>(t);
                }
            }

            @Override
            public <V> FunctionMatcher<T, V> caseOf(Predicate<T> predicate, Function<T, V> function) {
                if (predicate.test(t)) {
                    return new ResultFunctionMatcher<>(function.apply(t));
                } else {
                    return new FunctionMatcherImpl<>(t);
                }
            }

            @Override
            public <V> V orElse(Supplier<V> defSupplier) {
                return defSupplier == null ? null : defSupplier.get();
            }
        };
    }

    public interface StartFunctionMatcher<T> {
        <K, V> FunctionMatcher<T, V> typeOf(Class<K> kClass, Function<K, V> function);

        <K, V> FunctionMatcher<T, V> valueOf(K value, Function<K, V> function);

        <V> FunctionMatcher<T, V> caseOf(Predicate<T> predicate, Function<T, V> function);

        default <V> V orElse(V defValue) {
            return orElse(() -> defValue);
        }

        <V> V orElse(Supplier<V> defSupplier);
    }

    public interface FunctionMatcher<T, V> {
        <K> FunctionMatcher<T, V> typeOf(Class<K> kClass, Function<K, V> function);

        <K> FunctionMatcher<T, V> valueOf(K value, Function<K, V> function);

        FunctionMatcher<T, V> caseOf(Predicate<T> predicate, Function<T, V> function);

        default V orElse(V defValue) {
            return orElse(() -> defValue);
        }

        V orElse(Supplier<V> defSupplier);
    }

    private static class FunctionMatcherImpl<T, V> extends Matcher implements FunctionMatcher<T, V> {
        private final T t;

        public FunctionMatcherImpl(T t) {
            this.t = t;
        }

        @Override
        public <K> FunctionMatcher<T, V> typeOf(Class<K> kClass, Function<K, V> function) {
            if (kClass != null && kClass.isInstance(t)) {
                return new ResultFunctionMatcher<>(function.apply((K) t));
            } else {
                return new FunctionMatcherImpl<>(t);
            }
        }

        @Override
        public <K> FunctionMatcher<T, V> valueOf(K value, Function<K, V> function) {
            if (Objects.equals(value, t)) {
                return new ResultFunctionMatcher<>(function.apply((K) t));
            } else {
                return new FunctionMatcherImpl<>(t);
            }
        }

        @Override
        public FunctionMatcher<T, V> caseOf(Predicate<T> predicate, Function<T, V> function) {
            if (predicate.test(t)) {
                return new ResultFunctionMatcher<>(function.apply(t));
            } else {
                return new FunctionMatcherImpl<>(t);
            }
        }

        @Override
        public V orElse(Supplier<V> defSupplier) {
            return defSupplier.get();
        }
    }

    private static class ResultFunctionMatcher<T, V> extends Matcher implements FunctionMatcher<T, V> {
        private final V value;

        private ResultFunctionMatcher(V value) {
            this.value = value;
        }

        @Override
        public <K> FunctionMatcher<T, V> typeOf(Class<K> kClass, Function<K, V> function) {
            return this;
        }

        @Override
        public <K> FunctionMatcher<T, V> valueOf(K value, Function<K, V> function) {
            return this;
        }

        @Override
        public FunctionMatcher<T, V> caseOf(Predicate<T> predicate, Function<T, V> function) {
            return this;
        }

        @Override
        public V orElse(Supplier<V> defSupplier) {
            return value;
        }
    }

    public interface ConsumerMatcher<T> {
        <K> ConsumerMatcher<T> typeOf(Class<K> kClass, Consumer<K> consumer);

        <K> ConsumerMatcher<T> valueOf(K value, Consumer<K> consumer);

        ConsumerMatcher<T> caseOf(Predicate<T> predicate, Consumer<T> consumer);

        void orElse(Consumer<T> consumer);
    }

    private static class ConsumerMatcherImpl<T> extends Matcher implements ConsumerMatcher<T> {
        private final T t;

        public ConsumerMatcherImpl(T t) {
            this.t = t;
        }

        @Override
        public <K> ConsumerMatcher<T> typeOf(Class<K> kClass, Consumer<K> consumer) {
            if (kClass.isInstance(t)) {
                consumer.accept((K) t);
                return new EmptyConsumerMatcher<>();
            } else {
                return this;
            }
        }

        @Override
        public <K> ConsumerMatcher<T> valueOf(K value, Consumer<K> consumer) {
            if (Objects.equals(t, value)) {
                consumer.accept((K) t);
                return new EmptyConsumerMatcher<>();
            } else {
                return this;
            }
        }

        @Override
        public ConsumerMatcher<T> caseOf(Predicate<T> predicate, Consumer<T> consumer) {
            if (predicate.test(t)) {
                consumer.accept(t);
                return new EmptyConsumerMatcher<>();
            } else {
                return this;
            }
        }

        @Override
        public void orElse(Consumer<T> consumer) {
            consumer.accept(t);
        }
    }

    private static class EmptyConsumerMatcher<T> extends Matcher implements ConsumerMatcher<T> {

        @Override
        public <K> ConsumerMatcher<T> typeOf(Class<K> kClass, Consumer<K> consumer) {
            return this;
        }

        @Override
        public <K> ConsumerMatcher<T> valueOf(K value, Consumer<K> consumer) {
            return this;
        }

        @Override
        public ConsumerMatcher<T> caseOf(Predicate<T> predicate, Consumer<T> consumer) {
            return this;
        }

        @Override
        public void orElse(Consumer<T> consumer) {

        }
    }
}
