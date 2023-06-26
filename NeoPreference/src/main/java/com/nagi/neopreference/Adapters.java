package com.nagi.neopreference;

import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.*;
import java.util.stream.Collectors;

class Adapters {
    private Adapters() {

    }

    static class IntegerTypeAdapter extends TypeAdapter<Config.IntItem, Integer> {
        @Override
        public Property<Integer> createProperty(String key, Config.IntItem annotation, String preferenceName, SharedPreferences preferences) {
            Set<Integer> valueEnumSet = Arrays.stream(annotation.valueOf()).boxed().collect(Collectors.toSet());
            return new Property<>(key, preferenceName, preferences) {
                @Override
                String getValueString() {
                    return isEmpty() ? "empty int" : String.valueOf(get());
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
                            notifyAllListeners(value);
                        } else {
                            throw new IllegalArgumentException("value is invalid, must in values " + Arrays.toString(valueEnumSet.toArray()));
                        }
                    } else {
                        if (value > annotation.to() || value < annotation.start()) {
                            throw new IllegalArgumentException("value is invalid, must between " + annotation.to() + " and " + annotation.start());
                        } else {
                            getPreferences().edit().putInt(getKey(), value).apply();
                            notifyAllListeners(value);
                        }
                    }
                }
            };
        }
    }

    static class BooleanTypeAdapter extends TypeAdapter<Config.BooleanItem, Boolean> {
        @Override
        public Property<Boolean> createProperty(String key, Config.BooleanItem annotation, String preferenceName, SharedPreferences preferences) {
            return new Property<>(key, preferenceName, preferences) {
                @Override
                String getValueString() {
                    return isEmpty() ? "empty boolean" : String.valueOf(get());
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
                    notifyAllListeners(value);
                }
            };
        }
    }

    static class LongTypeAdapter extends TypeAdapter<Config.LongItem, Long> {

        @Override
        public Property<Long> createProperty(String key, Config.LongItem annotation, String preferenceName, SharedPreferences preferences) {
            Set<Long> valueEnumSet = Arrays.stream(annotation.valueOf()).boxed().collect(Collectors.toSet());
            return new Property<>(key, preferenceName, preferences) {
                @Override
                String getValueString() {
                    return isEmpty() ? "empty long" : String.valueOf(get());
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
                            notifyAllListeners(value);
                        } else {
                            throw new IllegalArgumentException("value is invalid, must in values " + Arrays.toString(valueEnumSet.toArray()));
                        }
                    } else {
                        if (value > annotation.to() || value < annotation.start()) {
                            throw new IllegalArgumentException("value is invalid, must between " + annotation.start() + " and " + annotation.to());
                        } else {
                            getPreferences().edit().putLong(getKey(), value).apply();
                            notifyAllListeners(value);
                        }
                    }

                }
            };
        }
    }

    static class FloatTypeAdapter extends TypeAdapter<Config.FloatItem, Float> {

        @Override
        public Property<Float> createProperty(String key, Config.FloatItem annotation, String preferenceName, SharedPreferences preferences) {
            Set<Float> valueEnumSet = new HashSet<>();
            for (Float v : annotation.valueOf()) {
                valueEnumSet.add(v);
            }
            return new Property<>(key, preferenceName, preferences) {
                @Override
                String getValueString() {
                    return isEmpty() ? "empty float" : String.valueOf(get());
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
                            notifyAllListeners(value);
                        } else {
                            throw new IllegalArgumentException("value is invalid, must in values " + Arrays.toString(valueEnumSet.toArray()));
                        }
                    } else {
                        if (value > annotation.to() || value < annotation.start()) {
                            throw new IllegalArgumentException("value is invalid, must between " + annotation.start() + " and " + annotation.to() + ", current is " + value);
                        } else {
                            getPreferences().edit().putFloat(getKey(), value).apply();
                            notifyAllListeners(value);
                        }
                    }
                }
            };
        }
    }

    static class StringTypeAdapter extends TypeAdapter<Config.StringItem, String> {

        @Override
        public Property<String> createProperty(String key, Config.StringItem annotation, String preferenceName, SharedPreferences preferences) {
            Set<String> valueEnumSet = Arrays.stream(annotation.valueOf()).collect(Collectors.toSet());
            return new Property<>(key, preferenceName, preferences) {
                @Override
                String getValueString() {
                    return isEmpty() ? "empty string" : String.valueOf(get());
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
                            notifyAllListeners(value);
                        } else {
                            throw new IllegalArgumentException("value is invalid, must in values " + Arrays.toString(valueEnumSet.toArray()));
                        }
                    } else {
                        if (!annotation.supportEmpty() && TextUtils.isEmpty(value)) {
                            throw new IllegalArgumentException("value is not support empty");
                        } else {
                            getPreferences().edit().putString(getKey(), value).apply();
                            notifyAllListeners(value);
                        }
                    }
                }
            };
        }
    }

    static class StringSetTypeAdapter extends TypeAdapter<Config.StringSetItem, Set<String>> {

        @Override
        public Property<Set<String>> createProperty(String key, Config.StringSetItem annotation, String preferenceName, SharedPreferences preferences) {
            Set<String> valueEnumSet = Arrays.stream(annotation.valueOf()).collect(Collectors.toSet());
            if (valueEnumSet.size() != annotation.valueOf().length) {
                throw new IllegalArgumentException("StringSetItem annotation contains duplication element:" + Arrays.toString(annotation.valueOf()));
            }
            return new Property<>(key, preferenceName, preferences) {
                @Override
                String getValueString() {
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
            };
        }
    }
}