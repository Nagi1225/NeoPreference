package com.nagi.preferencedemo;

import com.nagi.neopreference.Config;
import com.nagi.neopreference.Property;

import java.util.Set;

@Config.Name(DemoConfig.NAME)
public interface DemoConfig extends Config {
    String NAME = "demo_config";

    Property<Integer> intProperty();

    @StringItem(supportEmpty = true)
    Property<String> stringProperty();

    @FloatItem(key = "height")
    Property<Float> floatProperty();

    @LongItem(key = "last_save_time")
    Property<Long> longProperty();

    @BooleanItem
    Property<Boolean> boolProperty();

    @StringSetItem()
    Property<Set<String>> stringSetProperty();

    @StringSetItem(key = "collection_media_set", valueOf = {"mp3", "mp4", "png", "jpg", "mkv"})
    Property<Set<String>> collectMediaSet();

    @JsonData.JsonItem(key = "current_user_info")
    Property<UserInfo> userInfo();
}
