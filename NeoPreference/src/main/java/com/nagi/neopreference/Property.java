package com.nagi.neopreference;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.*;

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

    public interface Listener<T> {
        void onChanged(T newValue);
    }
}
