package com.nagi.neopreference;

import java.io.Serializable;
import java.util.Set;

@Config.Name(AppConfig.NAME)
public interface AppConfig extends Config {
    String NAME = "app_config";

    @IntItem(key = "version_code", start = 1, defaultValue = 1)
    Property<Integer> versionCode();

    @StringItem(supportEmpty = false, description = "it means app version, like 1.0.0")
    Property<String> versionName();

    @StringSetItem(key = "collection_media_set", valueOf = {"mp3", "mp4", "png", "jpg", "mkv"})
    Property<Set<String>> collectMediaSet();

    Property<UserInfo> userInfo();

    class UserInfo implements Serializable {
        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "UserInfo{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}

