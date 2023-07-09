# NeoPreference：一个简化SharedPreferences使用的工具

[![](https://jitpack.io/v/Nagi1225/NeoPreference.svg)](https://jitpack.io/#Nagi1225/NeoPreference)


## 新手入门

### 设置依赖

把下面配置添加到你的根目录`build.gradle`中在`repositories`的末尾：

```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

使用的模块中添加依赖：

```groovy
dependencies {
        implementation 'com.github.Nagi1225:NeoPreference:0.1.0' //以实际版本为准
}
```

### 简单使用

创建配置：

```java
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

    @StringSetItem(key = "collection_media_set", valueOf = {"mp3", "mp4", "png", "jpg", "mkv"})
    Property<Set<String>> collectMediaSet();
}
```

读写配置：

```java
// 1. Get config instance
DemoConfig config = ConfigManager.getInstance().getConfig(DemoConfig.class);
// 2. Get property value
float fp = config.floatProperty().get();
// 3. Get property value or default value
int num = config.intProperty().get(-1);
// 4. Determines whether the property value exists
boolean present = config.longProperty().isPresent();
// 5. Use Optional to handle property 
config.stringProperty().opt().ifPresent((String str) -> {
    //handle string property
});
```



## NeoPreference API 说明

这个工具的API除了`ConfigManager`类以外主要分两部分：`Property`类以及类型对应的注解。

### ConfigManager 接口说明

`ConfigManager`是单例实现，维护一个`SharedPreferences`和`Config`的注册表，提供`getConfig`和`addListener`两个方法。

以下是`getConfig`方法签名：

```java
public <P extends Config> P getConfig(Class<P> pClass);
public <P extends Config> P getConfig(Class<P> pClass, int mode);
```

参数`pClass`是继承`Config`类的接口`class`，可选参数`mode`对应`SharedPreferences`的`mode`。

`addListener`的方法监听指定`preferenceName`中内容的变化，签名如下：

```java
public void addListener(String preferenceName, Listener listener);
public void addListener(LifecycleOwner lifecycleOwner, String preferenceName, Listener listener);
public void removeListener(String preferenceName, Listener listener);
public void removeListeners(String preferenceName);
```

第一个方法接受一个`Listener`，需要手动调用`removeListener`或`removeListeners`，否则可能会内存溢出。第二个方法额外添加`LifecycleOwner`，这个监听器的声明周期采用`LifecycleOwner`对应的生命周期，在`onDestroy`时自动移除。

### Property 类接口说明

`Property`接口包括：

```java
public final String getKey();//获取属性对应的key
public T get(T defValue);    //获取属性值，defValue为默认值
public T get();              //获取属性值，采用缺省默认值
public void set(T value);    //设置属性值
public Optional<T> opt();    //以Optional的形式返回属性值
public boolean exists();     //判断属性当前是否存在，没有set过就是false，set后即便是null也为true
public final void addListener(Listener<T> listener)    //类似ConfigManager，不过只监听该属性的值变化，需要手动remove
public final void addListener(LifecycleOwner owner, Listener<T> listener)//类似ConfigManager，不过只监听该属性的值变化，在owner onDestroy时自动remove
```

泛型参数支持`Long`、`Integer`、`Float`、`Boolean`、`String`、`Set<String>`等`SharedPreferences`支持的几种类型。

### 类型相关注解介绍

这些注解对应`SharedPreferences`支持的几种类型（其中`description`字段暂时不用）。

```java
@interface StringItem {
    String key() default "";
    boolean supportEmpty() default true;
    String[] valueOf() default {};
    String defaultValue() default "";
    String description() default "";
}

@interface BooleanItem {
    String key() default "";
    boolean defaultValue() default false;
    String description() default "";
}

@interface IntItem {
    String key() default "";
    int defaultValue() default 0;
    int start() default Integer.MIN_VALUE;
    int to() default Integer.MAX_VALUE;
    int[] valueOf() default {};
    String description() default "";
}

@interface LongItem {
    String key() default "";
    long defaultValue() default 0;
    long start() default Long.MIN_VALUE;
    long to() default Long.MAX_VALUE;
    long[] valueOf() default {};
    String description() default "";
}

@interface FloatItem {
    String key() default "";
    float defaultValue() default 0;
    float start() default -Float.MAX_VALUE;
    float to() default Float.MAX_VALUE;
    float[] valueOf() default {};
    String description() default "";
}

@interface StringSetItem {
    String key() default "";
    String[] valueOf() default {};
    String description() default "";
}

@interface SerializableItem {
    String key() default "";
    Class<?> type() default Object.class;
    String description() default "";
}
```

## 扩展存储类型

除了`SharedPreferences`原本支持的类型外，可以通过`PropertyFactory`来扩展类型，例如我们想以json的格式存储JavaBean：

* 第一步：创建对应示意性接口和注解：

```java
public interface JsonData {

    @interface JsonItem {
        String key();

        String description() default "";
    }
}
```

* 第二步：创建对应的`PropertyFactory`并在使用前注册：


```java
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
    }
}
```

在使用前注册到`ConfigManager`：

```java
ConfigManager.registerFactory(new JsonPropertyFactory());
```

* 第三步：让对应数据类实现前面定义的接口：

```java
public class UserInfo implements JsonData{
    private String id;
    private String name;
    private int age;

    public UserInfo(String id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public UserInfo() {
    }

    //getter and setter...
}
```

这样就可以以json的格式存储JavaBean数据类了：

```java
@Config.Name(DemoConfig.NAME)
public interface DemoConfig extends Config {
    String NAME = "demo_config";

    @JsonData.JsonItem(key = "current_user_info")
    Property<UserInfo> userInfo();
}
```

```java
/* sample usage */
DemoConfig config = ConfigManager.getInstance().getConfig(DemoConfig.class);
config.userInfo().opt().ifPresentOrElse(userInfo -> {
    userInfo.setAge(userInfo.getAge() + 1);
    config.userInfo().set(userInfo);
    }, () -> {
    config.userInfo().set(new UserInfo("1", "Alice", 1));
});
```