package com.nagi.neopreference;

import android.content.SharedPreferences;
import android.text.TextUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

class Factories {
    @SuppressWarnings("rawtypes")
    private static final Map<Type, PropertyFactory> factoryMap = new HashMap<>();

    static {
        registerAdapter(new IntegerPropertyFactory());
        registerAdapter(new BooleanPropertyFactory());
        registerAdapter(new LongPropertyFactory());
        registerAdapter(new FloatPropertyFactory());
        registerAdapter(new StringPropertyFactory());
        registerAdapter(new StringSetPropertyFactory());
    }

    private Factories() {

    }

    @SuppressWarnings("rawtypes")
    static void registerAdapter(PropertyFactory factory) {
        Type type = factory.getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            if (actualTypeArguments.length != 2) {
                throw new IllegalArgumentException(String.format("Factory[%s] must have 2 type arguments, like PropertyFactory<Config.IntItem, Integer>",
                        factory.getClass().getCanonicalName()));
            } else {
                Type annoType = actualTypeArguments[0];
                Type valueType = actualTypeArguments[1];
                if (annoType instanceof Class && Annotation.class.isAssignableFrom((Class<?>) annoType)) {
                    factoryMap.put(valueType, factory);
                } else {
                    throw new IllegalArgumentException("Annotation Type Argument is not valid:" + annoType);
                }
            }
        } else {
            throw new IllegalArgumentException(String.format("Factory[%s] must contains type arguments, like PropertyFactory<Config.IntItem, Integer>",
                    factory.getClass().getCanonicalName()));
        }
    }

    @SuppressWarnings("rawtypes")
    static PropertyFactory getFactoryForType(Type type) {
        return Optional.ofNullable(factoryMap.get(type))
                .orElseGet(() -> {
                    Class<?> clazz = extractClass(type);
                    for (Type keyType : factoryMap.keySet()) {
                        Class<?> keyClass = extractClass(keyType);
                        if (keyClass.isAssignableFrom(clazz)) {
                            return factoryMap.get(keyType);
                        }
                    }
                    throw new IllegalStateException("can not found factory for type:" + type);
                });
    }

    private static Class<?> extractClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return extractClass(((ParameterizedType) type).getRawType());
        } else {
            throw new IllegalArgumentException("not support for type:" + type);
        }
    }

    private static String ensureKey(String key, String defaultKey) {
        return TextUtils.isEmpty(key) ? defaultKey : key;
    }

    static class IntegerPropertyFactory extends PropertyFactory<Config.IntItem, Integer> {
        @Override
        public Property<Integer> createProperty(String key, Config.IntItem annotation, String preferenceName, SharedPreferences preferences) {
            Set<Integer> valueEnumSet = Arrays.stream(annotation.valueOf()).boxed().collect(Collectors.toSet());
            return new Property.BaseProperty<>(ensureKey(annotation.key(), key), preferenceName, preferences) {
                @Override
                public String getValueString() {
                    return exists() ? String.valueOf(get()) : "empty int";
                }

                @Override
                public String getDescription() {
                    return annotation.description();
                }

                @Override
                public Integer get(Integer defValue) {
                    return getPreferences().getInt(getKey(), defValue);
                }

                @Override
                public Integer get() {
                    return get(annotation.defaultValue());
                }

                @Override
                public void set(Integer value) {
                    if (!valueEnumSet.isEmpty()) {
                        if (valueEnumSet.contains(value)) {
                            getPreferences().edit().putInt(getKey(), value).apply();
                        } else {
                            throw new IllegalArgumentException("value is invalid, must in values " + Arrays.toString(valueEnumSet.toArray()));
                        }
                    } else {
                        if (value > annotation.to() || value < annotation.start()) {
                            throw new IllegalArgumentException("value is invalid, must between " + annotation.to() + " and " + annotation.start());
                        } else {
                            getPreferences().edit().putInt(getKey(), value).apply();
                        }
                    }
                }

                @Override
                public Class<?> getValueClass() {
                    return Integer.class;
                }
            };
        }
    }

    static class BooleanPropertyFactory extends PropertyFactory<Config.BooleanItem, Boolean> {
        @Override
        public Property<Boolean> createProperty(String key, Config.BooleanItem annotation, String preferenceName, SharedPreferences preferences) {
            return new Property.BaseProperty<>(ensureKey(annotation.key(), key), preferenceName, preferences) {
                @Override
                public String getValueString() {
                    return exists() ? String.valueOf(get()) : "empty boolean";
                }

                @Override
                public String getDescription() {
                    return annotation.description();
                }

                @Override
                public Boolean get(Boolean defValue) {
                    return getPreferences().getBoolean(getKey(), defValue);
                }

                @Override
                public Boolean get() {
                    return get(annotation.defaultValue());
                }

                @Override
                public void set(Boolean value) {
                    getPreferences().edit().putBoolean(getKey(), value).apply();
                }

                @Override
                public Class<?> getValueClass() {
                    return Boolean.class;
                }
            };
        }
    }

    static class LongPropertyFactory extends PropertyFactory<Config.LongItem, Long> {

        @Override
        public Property<Long> createProperty(String key, Config.LongItem annotation, String preferenceName, SharedPreferences preferences) {
            Set<Long> valueEnumSet = Arrays.stream(annotation.valueOf()).boxed().collect(Collectors.toSet());
            return new Property.BaseProperty<>(ensureKey(annotation.key(), key), preferenceName, preferences) {
                @Override
                public String getValueString() {
                    return exists() ? String.valueOf(get()) : "empty long";
                }

                @Override
                public String getDescription() {
                    return annotation.description();
                }

                @Override
                public Long get(Long defValue) {
                    return getPreferences().getLong(getKey(), defValue);
                }

                @Override
                public Long get() {
                    return get(annotation.defaultValue());
                }

                @Override
                public void set(Long value) {
                    if (!valueEnumSet.isEmpty()) {
                        if (valueEnumSet.contains(value)) {
                            getPreferences().edit().putLong(getKey(), value).apply();
                        } else {
                            throw new IllegalArgumentException("value is invalid, must in values " + Arrays.toString(valueEnumSet.toArray()));
                        }
                    } else {
                        if (value > annotation.to() || value < annotation.start()) {
                            throw new IllegalArgumentException("value is invalid, must between " + annotation.start() + " and " + annotation.to());
                        } else {
                            getPreferences().edit().putLong(getKey(), value).apply();
                        }
                    }

                }

                @Override
                public Class<?> getValueClass() {
                    return Long.class;
                }
            };
        }
    }

    static class FloatPropertyFactory extends PropertyFactory<Config.FloatItem, Float> {

        @Override
        public Property<Float> createProperty(String key, Config.FloatItem annotation, String preferenceName, SharedPreferences preferences) {
            Set<Float> valueEnumSet = new HashSet<>();
            for (Float v : annotation.valueOf()) {
                valueEnumSet.add(v);
            }
            return new Property.BaseProperty<>(ensureKey(annotation.key(), key), preferenceName, preferences) {
                @Override
                public String getValueString() {
                    return exists() ? String.valueOf(get()) : "empty float";
                }

                @Override
                public String getDescription() {
                    return annotation.description();
                }

                @Override
                public Float get(Float defValue) {
                    return getPreferences().getFloat(getKey(), defValue);
                }

                @Override
                public Float get() {
                    return get(annotation.defaultValue());
                }

                @Override
                public void set(Float value) {
                    if (!valueEnumSet.isEmpty()) {
                        if (valueEnumSet.contains(value)) {
                            getPreferences().edit().putFloat(getKey(), value).apply();
                        } else {
                            throw new IllegalArgumentException("value is invalid, must in values " + Arrays.toString(valueEnumSet.toArray()));
                        }
                    } else {
                        if (value > annotation.to() || value < annotation.start()) {
                            throw new IllegalArgumentException("value is invalid, must between " + annotation.start() + " and " + annotation.to() + ", current is " + value);
                        } else {
                            getPreferences().edit().putFloat(getKey(), value).apply();
                        }
                    }
                }

                @Override
                public Class<?> getValueClass() {
                    return Float.class;
                }
            };
        }
    }

    static class StringPropertyFactory extends PropertyFactory<Config.StringItem, String> {

        @Override
        public Property<String> createProperty(String key, Config.StringItem annotation, String preferenceName, SharedPreferences preferences) {
            Set<String> valueEnumSet = Arrays.stream(annotation.valueOf()).collect(Collectors.toSet());
            return new Property.BaseProperty<>(ensureKey(annotation.key(), key), preferenceName, preferences) {
                @Override
                public String getValueString() {
                    return exists() ? String.valueOf(get()) : "empty string";
                }

                @Override
                public String getDescription() {
                    return annotation.description();
                }

                @Override
                public String get(String defValue) {
                    return getPreferences().getString(getKey(), defValue);
                }

                @Override
                public String get() {
                    return get(annotation.defaultValue());
                }

                @Override
                public void set(String value) {
                    if (!valueEnumSet.isEmpty()) {
                        if (valueEnumSet.contains(value)) {
                            getPreferences().edit().putString(getKey(), value).apply();
                        } else {
                            throw new IllegalArgumentException("value is invalid, must in values " + Arrays.toString(valueEnumSet.toArray()));
                        }
                    } else {
                        if (!annotation.supportEmpty() && TextUtils.isEmpty(value)) {
                            throw new IllegalArgumentException("value is not support empty");
                        } else {
                            getPreferences().edit().putString(getKey(), value).apply();
                        }
                    }
                }

                @Override
                public Class<?> getValueClass() {
                    return String.class;
                }
            };
        }
    }

    static class StringSetPropertyFactory extends PropertyFactory<Config.StringSetItem, Set<String>> {

        @Override
        public Property<Set<String>> createProperty(String key, Config.StringSetItem annotation, String preferenceName, SharedPreferences preferences) {
            Set<String> valueEnumSet = Arrays.stream(annotation.valueOf()).collect(Collectors.toSet());
            if (valueEnumSet.size() != annotation.valueOf().length) {
                throw new IllegalArgumentException("StringSetItem annotation contains duplication element:" + Arrays.toString(annotation.valueOf()));
            }
            return new Property.BaseProperty<>(ensureKey(annotation.key(), key), preferenceName, preferences) {
                @Override
                public String getValueString() {
                    return get().stream().reduce((s, s2) -> s + ", " + s2).orElse("empty str");
                }

                @Override
                public String getDescription() {
                    return annotation.description();
                }

                @Override
                public Set<String> get(Set<String> defValue) {
                    return getPreferences().getStringSet(getKey(), Collections.emptySet());
                }

                @Override
                public Set<String> get() {
                    return get(Collections.emptySet());
                }

                @Override
                public void set(Set<String> value) {
                    getPreferences().edit().putStringSet(
                                    getKey(),
                                    Optional.of(value)
                                            .filter(strings -> valueEnumSet.isEmpty() || valueEnumSet.containsAll(strings))
                                            .orElseThrow(() -> new IllegalArgumentException("string set contains invalid element:" +
                                                    value.stream().reduce((s, s2) -> s + ", " + s2).orElse(""))))
                            .apply();
                }

                @Override
                public Class<?> getValueClass() {
                    return Set.class;
                }
            };
        }
    }
}
