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

    public static void registerAdapter(TypeAdapter<?, ?> adapter) {
        Adapters.registerAdapter(adapter);
    }

    private ConfigManager() {

    }

    public static ConfigManager getInstance() {
        return sInstance;
    }

    private final HashMap<Class<? extends Config>, Config> preferenceMap = new HashMap<>();

    private final Map<String, Set<Listener>> listenerMap = new ConcurrentHashMap<>();

    public <P extends Config> P getConfig(Class<P> pClass) {
        return getConfig(pClass, Context.MODE_PRIVATE);
    }

    @SuppressWarnings("unchecked")
    public <P extends Config> P getConfig(Class<P> pClass, int mode) {
        return (P) Optional.ofNullable(preferenceMap.get(pClass))
                .orElseGet(() -> {
                    Config.Name nameAnnotation = pClass.getAnnotation(Config.Name.class);
                    String prefName = nameAnnotation == null ? pClass.getCanonicalName() : nameAnnotation.value();
                    Application application = Utils.getApp();
                    SharedPreferences preferences = application.getSharedPreferences(prefName, mode);
                    Map<String, Lazy<Property<?>>> map = Arrays.stream(pClass.getMethods())
                            .filter(method -> method.getReturnType().equals(Property.class))
                            .map(method -> new Pair<>(method.getName(), PropertyFactory.get(prefName, preferences, method)))
                            .collect(Collectors.toMap(pair -> pair.first, pair -> pair.second));
                    P preference = (P) Proxy.newProxyInstance(pClass.getClassLoader(), new Class[]{pClass}, (proxy, method, args) -> {
                        if (method.getReturnType().equals(Property.class)) {
                            return map.get(method.getName()).get();
                        } else if (method.getReturnType().equals(List.class) && method.getName().equals("getAll")) {
                            return map.values().stream()
                                    .map(Lazy::get)
                                    .collect(Collectors.toList());
                        } else {
                            throw new IllegalStateException(String.format("can not call method[%s]", method.getName()));
                        }
                    });
                    preferenceMap.put(pClass, preference);
                    return preference;
                });
    }

    public synchronized void addListener(String preferenceName, Listener listener) {
        Set<Listener> map = Optional.ofNullable(listenerMap.get(preferenceName)).orElseGet(HashSet::new);
        map.add(listener);
        listenerMap.put(preferenceName, map);
    }

    public synchronized void addListener(LifecycleOwner lifecycleOwner, String preferenceName, Listener listener) {
        Set<Listener> map = Optional.ofNullable(listenerMap.get(preferenceName)).orElseGet(HashSet::new);
        map.add(listener);
        listenerMap.put(preferenceName, map);
        lifecycleOwner.getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onDestroy(@NonNull LifecycleOwner owner) {
                DefaultLifecycleObserver.super.onDestroy(owner);
                synchronized (ConfigManager.this) {
                    map.remove(listener);
                }
            }
        });
    }

    public synchronized void removeListener(String preferenceName, Listener listener) {
        Set<Listener> map = Optional.ofNullable(listenerMap.get(preferenceName)).orElseGet(HashSet::new);
        map.remove(listener);
        if (map.isEmpty()) {
            listenerMap.remove(preferenceName);
        }
    }

    public synchronized void removePreferenceListeners(String preferenceName) {
        listenerMap.remove(preferenceName);
    }

    synchronized void notifyPreferenceListeners(String preferenceName, String key, Object value) {
        Optional.ofNullable(listenerMap.get(preferenceName))
                .ifPresent(set -> set.stream()
                        .forEach(listener -> listener.onPropertyChange(key, value)));
    }

    public interface Listener {
        void onPropertyChange(String key, Object value);
    }
}
