package com.nagi.neopreference;

import android.content.SharedPreferences;
import android.text.TextUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

public abstract class TypeAdapter<A extends Annotation, T> {
    public abstract Property<T> createProperty(String key, A annotation, String preferenceName, SharedPreferences preferences);
}