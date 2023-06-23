package com.nagi.neopreference;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import com.blankj.utilcode.util.Utils;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ConfigManager {

    private static final ConfigManager sInstance = new ConfigManager();

    private ConfigManager() {

    }

    public static ConfigManager getInstance() {
        return sInstance;
    }

    private final HashMap<Class<? extends Config>, Config> preferenceMap = new HashMap<>();

    private final Map<String, HashMap<Listener, Listener>> listenerMap = new ConcurrentHashMap<>();

    public <P extends Config> P getConfig(Class<P> pClass) {
        return getConfig(pClass, Context.MODE_PRIVATE);
    }

    public <P extends Config> P getConfig(Class<P> pClass, int mode) {
        return (P) Optional.ofNullable(preferenceMap.get(pClass))
                .orElseGet(() -> {
                    Config.Name nameAnnotation = pClass.getAnnotation(Config.Name.class);
                    String prefName = nameAnnotation == null ? pClass.getCanonicalName() : nameAnnotation.value();
                    Application application = Utils.getApp();
                    SharedPreferences preferences = application.getSharedPreferences(prefName, mode);
                    Map<String, PropertyFactory<?>> map = Arrays.stream(pClass.getMethods())
                            .filter(method -> method.getReturnType().equals(Property.class))
                            .map(method -> new Pair<>(method.getName(), LazyPropertyFactory.get(prefName, method)))
                            .collect(Collectors.toMap(pair -> pair.first, pair -> pair.second));
                    P preference = (P) Proxy.newProxyInstance(pClass.getClassLoader(), new Class[]{pClass}, (proxy, method, args) -> {
                        if (method.getReturnType().equals(Property.class)) {
                            return map.get(method.getName()).create(preferences);
                        } else if (method.getReturnType().equals(List.class) && method.getName().equals("getAll")) {
                            return map.values().stream()
                                    .map(propertyFactory -> propertyFactory.create(preferences))
                                    .collect(Collectors.toList());
                        } else {
                            throw new IllegalStateException(String.format("can not call method[%s]", method.getName()));
                        }
                    });
                    preferenceMap.put(pClass, preference);
                    return preference;
                });
    }

    public void addListener(String preferenceName, Listener listener) {
        HashMap<Listener, Listener> map = Optional.ofNullable(listenerMap.get(preferenceName)).orElseGet(HashMap::new);
        map.put(listener, listener);
        listenerMap.put(preferenceName, map);
    }

    public void addListener(LifecycleOwner lifecycleOwner, String preferenceName, Listener listener) {
        HashMap<Listener, Listener> map = Optional.ofNullable(listenerMap.get(preferenceName)).orElseGet(HashMap::new);
        map.put(listener, listener);
        listenerMap.put(preferenceName, map);
        lifecycleOwner.getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onDestroy(@NonNull LifecycleOwner owner) {
                DefaultLifecycleObserver.super.onDestroy(owner);
                map.remove(listener);
            }
        });
    }

    public void removeListener(String preferenceName, Listener listener) {
        HashMap<Listener, Listener> map = Optional.ofNullable(listenerMap.get(preferenceName)).orElseGet(HashMap::new);
        map.remove(listener);
        if (map.isEmpty()) {
            listenerMap.remove(preferenceName);
        }
    }

    public void removePreferenceListeners(String preferenceName) {
        listenerMap.remove(preferenceName);
    }

    void notifyPreferenceListeners(String preferenceName, String key, Object value) {
        Optional.ofNullable(listenerMap.get(preferenceName))
                .ifPresent(map -> map.values().stream()
                        .forEach(listener -> listener.onPropertyChange(key, value)));
    }

    void notifyAllListeners(String key, Object value) {
        listenerMap.values().stream()
                .forEach(map -> map.values().stream()
                        .forEach(listener -> listener.onPropertyChange(key, value)));
    }

    public interface Listener {
        void onPropertyChange(String key, Object value);
    }
}
