package com.nagi.neopreference;

import android.content.SharedPreferences;
import androidx.annotation.Keep;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PropertyFactory {
    static Lazy<Property<?>> get(String preferenceName, SharedPreferences preferences, Method method) {
        if (method.getParameterTypes().length == 0) {
            Type returnType = method.getGenericReturnType();
            if (returnType instanceof ParameterizedType) {
                Type[] types = ((ParameterizedType) returnType).getActualTypeArguments();
                if (types.length == 1) {
                    Type valueType = types[0];
                    String defaultKey = method.getName();
                    checkAnnotation(method, valueType, method.getAnnotations());
                    return Lazy.from(Matcher.matchFrom(valueType)
                            .valueOf(Integer.class, intClass -> (Supplier<Property<?>>)
                                    () -> new Property.IntProperty(defaultKey, extractAnnotation(method, Config.IntItem.class), preferenceName, preferences))
                            .valueOf(Float.class, floatClass ->
                                    () -> new Property.FloatProperty(defaultKey, extractAnnotation(method, Config.FloatItem.class), preferenceName, preferences))
                            .valueOf(String.class, stringClass ->
                                    () -> new Property.StringProperty(defaultKey, extractAnnotation(method, Config.StringItem.class), preferenceName, preferences))
                            .valueOf(Boolean.class, booleanClass ->
                                    () -> new Property.BooleanProperty(defaultKey, extractAnnotation(method, Config.BooleanItem.class), preferenceName, preferences))
                            .valueOf(Long.class, longClass ->
                                    () -> new Property.LongProperty(defaultKey, extractAnnotation(method, Config.LongItem.class), preferenceName, preferences))
                            .typeOf(ParameterizedType.class, parameterizedType -> {
                                Type rawType = ((ParameterizedType) valueType).getRawType();
                                Type[] actualTypeArguments = ((ParameterizedType) valueType).getActualTypeArguments();
                                if (Objects.equals(rawType, Set.class) && Objects.equals(actualTypeArguments[0], String.class)) {
                                    return () -> new Property.StringSetProperty(defaultKey, extractAnnotation(method, Config.StringSetItem.class), preferenceName, preferences);
                                } else {
                                    throw new RuntimeException("error parameterizedType:" + rawType + " - " + Arrays.toString(actualTypeArguments));
                                }
                            })
                            .orElse((Supplier<Supplier<Property<?>>>) () -> {
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
        List<Class<?>> annotationList = Arrays.stream(annotations)
                .map(Annotation::annotationType)
                .filter(Config.ITEM_ANNOTATION_MAP::containsKey)
                .collect(Collectors.toList());

        int annotationSize = annotationList.size();
        String methodName = method.getName();
        if (annotationSize > 1) {
            throw new IllegalStateException(String.format("method \"%s\" contains more than one Preference annotation!", methodName));
        } else if (annotationSize == 1) {
            Class<?> annoClass = annotationList.get(0);
            Class<?> vClass = Config.ITEM_ANNOTATION_MAP.get(annoClass);
            if (vClass == null) {
                throw new IllegalStateException(String.format("property \"%s\" has wrong type: %s", methodName, valueType));
            } else {
                if (valueType instanceof Class && Objects.equals(vClass, valueType)) {
                    //safe
                } else if (valueType instanceof ParameterizedType && Objects.equals(vClass, ((ParameterizedType) valueType).getRawType())) {
                    //safe
                } else {
                    throw new IllegalStateException(String.format("property \"%s\" which is %s has wrong Preference annotation: %s", methodName, valueType, annoClass.getName()));
                }
            }
        } else {
            //no annotation
        }
    }


    @SuppressWarnings("unchecked")
    private static <A extends Annotation> A extractAnnotation(Method method, Class<? extends A> aClass) {
        return (A) Optional.ofNullable((Annotation) method.getAnnotation(aClass))
                .orElseGet(() -> DefaultConfig.DEFAULT_ANNOTATION_MAP.get(aClass));
    }

    @Keep
    private interface DefaultConfig {
        Map<Class<?>, Annotation> DEFAULT_ANNOTATION_MAP = Arrays.stream(DefaultConfig.class.getMethods())
                .filter(method -> method.getReturnType().equals(Property.class))
                .map(AccessibleObject::getAnnotations)
                .flatMap(Arrays::stream)
                .collect(Collectors.toMap(Annotation::annotationType, annotation -> annotation));

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

        @Config.StringSetItem
        Property<Set<String>> stringSetProperty();
    }
}
