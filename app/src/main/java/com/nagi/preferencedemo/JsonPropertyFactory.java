package com.nagi.preferencedemo;

import android.content.SharedPreferences;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.nagi.neopreference.Property;
import com.nagi.neopreference.PropertyFactory;

public class JsonPropertyFactory extends PropertyFactory<JsonData.JsonItem, JsonData> {
    private final static Gson gson = new Gson();

    @Override
    public Property<JsonData> createProperty(String key, JsonData.JsonItem annotation, String preferenceName, SharedPreferences preferences) {
        return new JsonProperty<>(key, annotation, preferenceName, preferences);
    }

    static class JsonProperty<T extends JsonData> extends Property.BaseProperty<T> {
        final String description;

        public JsonProperty(String key, JsonData.JsonItem annotation, String preferenceName, SharedPreferences preferences) {
            super(annotation != null ? annotation.key() : key, preferenceName, preferences);
            description = annotation != null ? annotation.description() : "no description";
        }

        @Override
        public String getValueString() {
            return getPreferences().getString(getKey(), "");
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public T get(T defValue) {
            String data = getPreferences().getString(getKey(), "");
            if (TextUtils.isEmpty(data)) {
                return defValue;
            } else {
                int classEndIndex = data.indexOf(":");
                try {
                    Class<?> clazz = Class.forName(data.substring(0, classEndIndex));
                    String jsonStr = data.substring(classEndIndex + 1);
                    return (T) gson.fromJson(jsonStr, clazz);
                } catch (ClassNotFoundException | JsonSyntaxException e) {
                    throw new RuntimeException("parse data failed:", e);
                }
            }
        }

        @Override
        public T get() {
            return get(null);
        }

        @Override
        public void set(T value) {
            String className = value.getClass().getCanonicalName();
            getPreferences().edit().putString(getKey(), className + ":" + gson.toJson(value)).apply();
        }

        @Override
        public Class<?> getValueClass() {
            return JsonData.class;
        }
    }
}
