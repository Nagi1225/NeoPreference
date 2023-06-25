package com.nagi.neopreference;

import android.content.SharedPreferences;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Property<T> {
    private final String preferenceName;
    private final String key;
    private final SharedPreferences preferences;
    private final Map<Listener<T>, Listener<T>> listenerMap = new HashMap<>();

    Property(String key, String preferenceName, SharedPreferences preferences) {
        this.key = key;
        this.preferenceName = preferenceName;
        this.preferences = preferences;
    }

    public synchronized final void addListener(Listener<T> listener) {
        listenerMap.put(listener, listener);
    }

    public synchronized final void addListener(LifecycleOwner owner, Listener<T> listener) {
        listenerMap.put(listener, listener);
        owner.getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onDestroy(@NonNull LifecycleOwner owner) {
                DefaultLifecycleObserver.super.onDestroy(owner);
                listenerMap.remove(listener, listener);
            }
        });
    }

    public synchronized final void removeListener(Listener<T> listener) {
        listenerMap.remove(listener);
    }

    protected synchronized final void notifyAllListeners(T value) {
        for (Listener<T> listener : listenerMap.values()) {
            listener.onChanged(value);
        }
        ConfigManager.getInstance().notifyPreferenceListeners(getPreferenceName(), getKey(), value);
    }

    public final String getKey() {
        return key;
    }

    protected final String getPreferenceName() {
        return preferenceName;
    }

    protected final SharedPreferences getPreferences() {
        return preferences;
    }

    public Optional<T> opt() {
        return isPresent()
                ? Optional.ofNullable(get())
                : Optional.empty();
    }

    abstract String getValueString();

    public abstract String getDescription();

    public abstract T get(T defValue);

    public abstract T get();

    public abstract void set(T value);

    public boolean isPresent() {
        return preferences.contains(getKey());
    }

    public boolean isEmpty() {
        return !isPresent();
    }

    private static String ensureKey(String key, String defaultKey) {
        return TextUtils.isEmpty(key) ? defaultKey : key;
    }

    static class IntProperty extends Property<Integer> {
        final Config.IntItem annotation;
        private final Set<Integer> valueEnumSet;

        IntProperty(String key, Config.IntItem annotation, String preferenceName, SharedPreferences preferences) {
            super(ensureKey(annotation.key(), key), preferenceName, preferences);
            this.annotation = annotation;
            valueEnumSet = Arrays.stream(annotation.valueOf()).boxed().collect(Collectors.toSet());
        }

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
    }

    static class BooleanProperty extends Property<Boolean> {
        private final Config.BooleanItem annotation;

        BooleanProperty(String key, Config.BooleanItem annotation, String preferenceName, SharedPreferences preferences) {
            super(ensureKey(annotation.key(), key), preferenceName, preferences);
            this.annotation = annotation;
        }

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
    }

    static class LongProperty extends Property<Long> {
        private final Config.LongItem annotation;
        private final Set<Long> valueEnumSet;

        LongProperty(String key, Config.LongItem annotation, String preferenceName, SharedPreferences preferences) {
            super(ensureKey(annotation.key(), key), preferenceName, preferences);
            this.annotation = annotation;
            valueEnumSet = Arrays.stream(annotation.valueOf()).boxed().collect(Collectors.toSet());
        }

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
    }

    static class FloatProperty extends Property<Float> {
        private final Config.FloatItem annotation;
        private final Set<Float> valueEnumSet;

        FloatProperty(String key, Config.FloatItem annotation, String preferenceName, SharedPreferences preferences) {
            super(ensureKey(annotation.key(), key), preferenceName, preferences);
            this.annotation = annotation;
            Set<Float> set = new HashSet<>();
            for (Float v : annotation.valueOf()) {
                set.add(v);
            }
            valueEnumSet = Collections.unmodifiableSet(set);
        }

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
    }

    static class StringProperty extends Property<String> {
        private final Config.StringItem annotation;
        private final Set<String> valueEnumSet;

        StringProperty(String key, Config.StringItem annotation, String preferenceName, SharedPreferences preferences) {
            super(ensureKey(annotation.key(), key), preferenceName, preferences);
            this.annotation = annotation;
            valueEnumSet = Arrays.stream(annotation.valueOf()).collect(Collectors.toSet());
        }

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
    }

    static class StringSetProperty extends Property<Set<String>> {

        private final Config.StringSetItem annotation;
        private final Set<String> valueEnumSet;

        StringSetProperty(String key, Config.StringSetItem annotation, String preferenceName, SharedPreferences preferences) {
            super(ensureKey(annotation.key(), key), preferenceName, preferences);
            this.annotation = annotation;
            valueEnumSet = Arrays.stream(annotation.valueOf())
                    .collect(Collectors.toSet());
            if (valueEnumSet.size() != annotation.valueOf().length) {
                throw new IllegalArgumentException("StringSetItem annotation contains duplication element:" + Arrays.toString(annotation.valueOf()));
            }
        }

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
    }

    public interface Listener<T> {
        void onChanged(T newValue);
    }
}
