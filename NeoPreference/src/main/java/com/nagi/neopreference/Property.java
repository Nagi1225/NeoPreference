package com.nagi.neopreference;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.*;

public interface Property<T> {
    default Optional<T> opt() {
        return isPresent()
                ? Optional.ofNullable(get())
                : Optional.empty();
    }

    String getValueString();

    String getDescription();

    T get(T defValue);

    T get();

    void set(T value);

    String getKey();

    String getPreferenceName();

    boolean isPresent();

    default boolean isEmpty() {
        return !isPresent();
    }

    void addListener(Listener<T> listener);

    void addListener(LifecycleOwner owner, Listener<T> listener);

    void removeListener(Listener<T> listener);

    abstract class BaseProperty<T> implements Property<T> {
        private final String preferenceName;
        private final String key;
        private final SharedPreferences preferences;

        BaseProperty(String key, String preferenceName, SharedPreferences preferences) {
            this.key = key;
            this.preferenceName = preferenceName;
            this.preferences = preferences;
        }

        @Override
        public final void addListener(Listener<T> listener) {
            throw new IllegalStateException("not implement");
        }

        @Override
        public final void addListener(LifecycleOwner owner, Listener<T> listener) {
            throw new IllegalStateException("not implement");
        }

        @Override
        public final void removeListener(Listener<T> listener) {
            throw new IllegalStateException("not implement");
        }

        @Override
        public final String getKey() {
            return key;
        }

        @Override
        public final String getPreferenceName() {
            return preferenceName;
        }

        protected final SharedPreferences getPreferences() {
            return preferences;
        }

        @Override
        public final boolean isPresent() {
            return preferences.contains(getKey());
        }
    }

    interface Listener<T> {
        void onChanged(T newValue);
    }
}

final class PropertyWrapper<T> implements Property<T> {
    final Property<T> impl;
    private final Set<Listener<T>> listenerSet = Collections.synchronizedSet(new HashSet<>());

    public PropertyWrapper(Property<T> impl) {
        this.impl = impl;
    }

    @Override
    public String getValueString() {
        return impl.getValueString();
    }

    @Override
    public String getDescription() {
        return impl.getDescription();
    }

    @Override
    public T get(T defValue) {
        return impl.get(defValue);
    }

    @Override
    public T get() {
        return impl.get();
    }

    @Override
    public void set(T value) {
        impl.set(value);
        notifyAllListeners(value);
    }

    @Override
    public String getKey() {
        return impl.getKey();
    }

    @Override
    public String getPreferenceName() {
        return impl.getPreferenceName();
    }

    @Override
    public boolean isPresent() {
        return impl.isPresent();
    }

    @Override
    public void addListener(Listener<T> listener) {
        listenerSet.add(listener);
    }

    @Override
    public void addListener(LifecycleOwner owner, Listener<T> listener) {
        listenerSet.add(listener);
        owner.getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onDestroy(@NonNull LifecycleOwner owner) {
                DefaultLifecycleObserver.super.onDestroy(owner);
                listenerSet.remove(listener);
            }
        });
    }

    @Override
    public void removeListener(Listener<T> listener) {
        listenerSet.remove(listener);
    }

    private synchronized void notifyAllListeners(T value) {
        synchronized (listenerSet) {
            for (Listener<T> listener : listenerSet) {
                listener.onChanged(value);
            }
        }
        ConfigManager.getInstance().notifyPreferenceListeners(getPreferenceName(), getKey(), value);
    }
}


