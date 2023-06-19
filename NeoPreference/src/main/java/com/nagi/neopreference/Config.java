package com.nagi.neopreference;

import androidx.annotation.Keep;

import java.lang.annotation.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Keep
public interface Config {

    List<Property<?>> getAll();

    Set<Class<? extends Annotation>> ITEM_ANNOTATION_MAP = Collections
            .unmodifiableSet(Arrays
                    .asList(StringItem.class,
                            IntItem.class,
                            BooleanItem.class,
                            FloatItem.class,
                            LongItem.class,
                            StringSetItem.class,
                            SerializableItem.class)
                    .stream()
                    .collect(Collectors.toSet()));

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

        float start() default -Float.MIN_VALUE;

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

    @Target(value = ElementType.METHOD)
    @Retention(value = RetentionPolicy.RUNTIME)
    @Inherited
    @interface SerializableItem {
        String key() default "";

        Class<?> type() default Object.class;

        String description() default "";
    }
}
