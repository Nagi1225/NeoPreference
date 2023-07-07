package com.nagi.neopreference;

import android.util.Pair;
import androidx.annotation.Keep;

import java.lang.annotation.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Keep
public interface Config {

    List<Property<?>> getAll();

    Map<Class<? extends Annotation>, Class<?>> ITEM_ANNOTATION_MAP = Collections.unmodifiableMap(Stream
            .of(new Pair<>(StringItem.class, String.class),
                    new Pair<>(IntItem.class, Integer.class),
                    new Pair<>(BooleanItem.class, Boolean.class),
                    new Pair<>(FloatItem.class, Float.class),
                    new Pair<>(LongItem.class, Long.class),
                    new Pair<>(StringSetItem.class, Set.class))
            .collect(Collectors.toMap(p -> p.first, p -> p.second)));

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Name {
        String value();
    }

    @Target(value = ElementType.METHOD)
    @Retention(value = RetentionPolicy.RUNTIME)
    @Inherited
    @interface StringItem {
        String key() default "";

        boolean supportEmpty() default true;

        String[] valueOf() default {};

        String defaultValue() default "";

        String description() default "";
    }

    @Target(value = ElementType.METHOD)
    @Retention(value = RetentionPolicy.RUNTIME)
    @Inherited
    @interface BooleanItem {
        String key() default "";

        boolean defaultValue() default false;

        String description() default "";
    }

    @Target(value = ElementType.METHOD)
    @Retention(value = RetentionPolicy.RUNTIME)
    @Inherited
    @interface IntItem {
        String key() default "";

        int defaultValue() default 0;

        int start() default Integer.MIN_VALUE;

        int to() default Integer.MAX_VALUE;

        int[] valueOf() default {};

        String description() default "";
    }

    @Target(value = ElementType.METHOD)
    @Retention(value = RetentionPolicy.RUNTIME)
    @Inherited
    @interface LongItem {
        String key() default "";

        long defaultValue() default 0;

        long start() default Long.MIN_VALUE;

        long to() default Long.MAX_VALUE;

        long[] valueOf() default {};

        String description() default "";
    }

    @Target(value = ElementType.METHOD)
    @Retention(value = RetentionPolicy.RUNTIME)
    @Inherited
    @interface FloatItem {
        String key() default "";

        float defaultValue() default 0;

        float start() default -Float.MAX_VALUE;

        float to() default Float.MAX_VALUE;

        float[] valueOf() default {};

        String description() default "";
    }

    @Target(value = ElementType.METHOD)
    @Retention(value = RetentionPolicy.RUNTIME)
    @Inherited
    @interface StringSetItem {
        String key() default "";

        String[] valueOf() default {};

        String description() default "";
    }
}
