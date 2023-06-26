package com.nagi.neopreference;

import android.content.SharedPreferences;
import android.text.TextUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public abstract class TypeAdapter<A extends Annotation, T> {
    public abstract Property<T> createProperty(String key, A annotation, String preferenceName, SharedPreferences preferences);

    final Class<? extends Annotation> getTypeAnnotationClass() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
            if (types.length == 2) {
                return (Class<? extends Annotation>) types[0];
            } else {
                throw new IllegalStateException("ActualTypeArguments length is not 2");
            }
        } else {
            throw new IllegalStateException("genericSuperclass is not ParameterizedType");
        }
    }
}