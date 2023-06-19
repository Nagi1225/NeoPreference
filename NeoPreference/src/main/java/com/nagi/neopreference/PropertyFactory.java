package com.nagi.neopreference;

import android.content.SharedPreferences;
import androidx.annotation.Keep;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public interface PropertyFactory<T> {

    Property<T> create(SharedPreferences preferences);
}

class LazyPropertyFactory<T> implements PropertyFactory<T> {
    private final PropertyFactory<T> factory;
    private Property<T> property;

    LazyPropertyFactory(PropertyFactory<T> factory) {
        this.factory = factory;
    }

    @Override
    public Property<T> create(SharedPreferences preferences) {
        if (property == null) {
            synchronized (this) {
                if (property == null) {
                    property = factory.create(preferences);
                }
            }
        }
        return property;
    }

    static PropertyFactory<?> get(Method method) {
        if (method.getParameterTypes().length == 0) {
            Type returnType = method.getGenericReturnType();
            if (returnType instanceof ParameterizedType) {
                Type[] types = ((ParameterizedType) returnType).getActualTypeArguments();
                if (types.length == 1) {
                    Type valueType = types[0];
                    String defaultKey = method.getName();
                    checkAnnotation(method, valueType, method.getAnnotations());
                    return new LazyPropertyFactory(Matcher.matchFrom(valueType)
                            .valueOf(Integer.class, intClass -> (PropertyFactory)
                                    preferences -> new Property.IntProperty(defaultKey, extractAnnotation(method, Config.IntItem.class), preferences))
                            .valueOf(Float.class, floatClass ->
                                    preferences -> new Property.FloatProperty(defaultKey, extractAnnotation(method, Config.FloatItem.class), preferences))
                            .valueOf(String.class, stringClass ->
                                    preferences -> new Property.StringProperty(defaultKey, extractAnnotation(method, Config.StringItem.class), preferences))
                            .valueOf(Boolean.class, booleanClass ->
                                    preferences -> new Property.BooleanProperty(defaultKey, extractAnnotation(method, Config.BooleanItem.class), preferences))
                            .valueOf(Long.class, longClass ->
                                    preferences -> new Property.LongProperty(defaultKey, extractAnnotation(method, Config.LongItem.class), preferences))
                            .valueOf(Serializable.class, serializableClass ->
                                    preferences -> new Property.SerializableProperty<>(defaultKey, extractAnnotation(method, Config.SerializableItem.class), preferences))
                            .typeOf(ParameterizedType.class, parameterizedType -> {
                                Type rawType = ((ParameterizedType) valueType).getRawType();
                                Type[] actualTypeArguments = ((ParameterizedType) valueType).getActualTypeArguments();
                                if (Objects.equals(rawType, Set.class) && Objects.equals(actualTypeArguments[0], String.class)) {
                                    return preferences -> new Property.StringSetProperty(defaultKey, extractAnnotation(method, Config.StringSetItem.class), preferences);
                                } else {
                                    throw new RuntimeException("error parameterizedType:" + rawType + " - " + Arrays.toString(actualTypeArguments));
                                }
                            })
                            .caseOf(type -> type instanceof Class && Serializable.class.isAssignableFrom((Class<?>) type), type ->
                                    preferences -> new Property.SerializableProperty<>(defaultKey, extractAnnotation(method, Config.SerializableItem.class), preferences))
                            .orElse(() -> {
                                throw new RuntimeException("error returnType:" + valueType);
                            }));
                } else {
                    throw new IllegalStateException("type arguments length != 1");
                }
            } else {
                throw new IllegalStateException("type is not ParameterizedType");
            }
        } else {
            throw new IllegalArgumentException(String.format("%s.%s's parameter must be empty", method.getDeclaringClass().getCanonicalName(), method.getName()));
        }
    }

    private static void checkAnnotation(Method method, Type valueType, Annotation[] annotations) {
        List<Class<?>> annotationList = Collections.unmodifiableList(Arrays.stream(annotations)
                .map(Object::getClass)
                .filter(Config.ITEM_ANNOTATION_MAP::contains)
                .collect(Collectors.toList()));

        int annotationSize = annotationList.size();
        String methodName = method.getName();
        if (annotationSize > 1) {
            throw new IllegalStateException(String.format("method \"%s\" contains more than one Preference annotation!", methodName));
        } else if (annotationSize == 1) {
            Class<?> annoClass = annotationList.get(0);
            if (valueType.equals(Integer.class) && !annotationList.contains(Config.IntItem.class)) {
                throw new IllegalStateException(String.format("property \"%s\" which is Integer has wrong Preference annotation: %s", methodName, annoClass.getName()));
            } else if (valueType.equals(String.class) && !annotationList.contains(Config.StringItem.class)) {
                throw new IllegalStateException(String.format("property \"%s\" which is String has wrong Preference annotation: %s", methodName, annoClass.getName()));
            } else if (valueType.equals(Boolean.class) && !annotationList.contains(Config.BooleanItem.class)) {
                throw new IllegalStateException(String.format("property \"%s\" which is Boolean has wrong Preference annotation: %s", methodName, annoClass.getName()));
            } else if (valueType.equals(Float.class) && !annotationList.contains(Config.FloatItem.class)) {
                throw new IllegalStateException(String.format("property \"%s\" which is Float has wrong Preference annotation: %s", methodName, annoClass.getName()));
            } else if (valueType.equals(Long.class) && !annotationList.contains(Config.LongItem.class)) {
                throw new IllegalStateException(String.format("property \"%s\" which is Long has wrong Preference annotation: %s", methodName, annoClass.getName()));
            } else if (valueType.equals(Set.class) && !annotationList.contains(Config.StringSetItem.class)) {
                throw new IllegalStateException(String.format("property \"%s\" which is Set<String> has wrong Preference annotation: %s", methodName, annoClass.getName()));
            } else if (valueType.equals(Serializable.class) && !annotationList.contains(Config.SerializableItem.class)) {
                throw new IllegalStateException(String.format("property \"%s\" which is Serializable has wrong Preference annotation: %s", methodName, annoClass.getName()));
            } else {
                throw new IllegalStateException(String.format("property \"%s\" has wrong type: %s", methodName, valueType));
            }
        }
    }


    private static <A extends Annotation> A extractAnnotation(Method method, Class<? extends A> aClass) {
        return (A) Optional.ofNullable((Annotation) method.getAnnotation(aClass))
                .orElseGet(() -> {
                    try {
                        if (aClass.equals(Config.IntItem.class)) {
                            return DefaultConfig.class.getMethod("intProperty").getAnnotation(aClass);
                        } else if (aClass.equals(Config.FloatItem.class)) {
                            return DefaultConfig.class.getMethod("floatProperty").getAnnotation(aClass);
                        } else if (aClass.equals(Config.LongItem.class)) {
                            return DefaultConfig.class.getMethod("longProperty").getAnnotation(aClass);
                        } else if (aClass.equals(Config.BooleanItem.class)) {
                            return DefaultConfig.class.getMethod("booleanProperty").getAnnotation(aClass);
                        } else if (aClass.equals(Config.StringItem.class)) {
                            return DefaultConfig.class.getMethod("stringProperty").getAnnotation(aClass);
                        } else if (aClass.equals(Config.SerializableItem.class)) {
                            return DefaultConfig.class.getMethod("serializableProperty").getAnnotation(aClass);
                        } else if (aClass.equals(Config.StringSetItem.class)) {
                            return DefaultConfig.class.getMethod("stringSetProperty").getAnnotation(aClass);
                        } else {
                            throw new IllegalStateException("not support class:" + aClass);
                        }
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Keep
    interface DefaultConfig {
        @Config.IntItem
        Property<Integer> intProperty();

        @Config.FloatItem
        Property<Float> floatProperty();

        @Config.LongItem
        Property<Long> longProperty();

        @Config.BooleanItem
        Property<Boolean> booleanProperty();

        @Config.StringItem
        Property<String> stringProperty();

        @Config.SerializableItem
        Property<Serializable> serializableProperty();

        @Config.StringSetItem
        Property<Set<String>> stringSetProperty();
    }
}
