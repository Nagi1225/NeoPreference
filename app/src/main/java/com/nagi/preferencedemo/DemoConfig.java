package com.nagi.preferencedemo;

import com.nagi.neopreference.Config;
import com.nagi.neopreference.Property;

import java.util.Set;

@Config.Name(DemoConfig.NAME)
public interface DemoConfig extends Config {
    String NAME = "demo_config";

    @IntItem(key = "app_open_count", description = "应用打开次数")
    Property<Integer> intProperty();

    @StringItem(key = "user_id", description = "用户id")
    Property<String> stringProperty();

    @FloatItem(key = "height", description = "xx高度")
    Property<Float> floatProperty();

    @LongItem(key = "last_save_time", description = "上一次保存时间")
    Property<Long> longProperty();

    @BooleanItem(key = "is_first_open", defaultValue = true, description = "应用是否第一次启动")
    Property<Boolean> boolProperty();

    @StringSetItem(key = "collection_media_set", valueOf = {"mp3", "mp4", "png", "jpg", "mkv"})
    Property<Set<String>> collectMediaSet();

    @JsonData.JsonItem(key = "current_user_info")
    Property<UserInfo> userInfo();
}
