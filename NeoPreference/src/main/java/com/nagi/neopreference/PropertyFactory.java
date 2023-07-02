package com.nagi.neopreference;

import android.content.SharedPreferences;
import androidx.annotation.Keep;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public abstract class PropertyFactory<A extends Annotation, T> {

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

    @SuppressWarnings({"unchecked", "rawtypes"})
    static Lazy<Property<?>> get(String preferenceName, SharedPreferences preferences, Method method) {
        if (method.getParameterTypes().length == 0) {
            Type returnType = method.getGenericReturnType();
            if (returnType instanceof ParameterizedType) {
                Type[] types = ((ParameterizedType) returnType).getActualTypeArguments();
                if (types.length == 1) {
                    Type valueType = types[0];
                    String defaultKey = method.getName();
                    checkAnnotation(method, valueType, method.getAnnotations());
                    PropertyFactory factory = Factories.getFactoryForType(valueType);
                    if (factory == null) {
                        throw new RuntimeException("error returnType:" + valueType);
                    } else {
                        return Lazy.from(() -> new PropertyWrapper<Object>(
                                        factory.createProperty(defaultKey, extractAnnotation(method, factory.getTypeAnnotationClass()), preferenceName, preferences)));
                    }
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
