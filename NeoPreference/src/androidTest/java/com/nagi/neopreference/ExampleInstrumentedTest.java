package com.nagi.neopreference;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.Keep;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    public static final String TEST_CONFIG_NAME = "test_config";

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.nagi.neopreference.test", appContext.getPackageName());
    }

    @Test
    public void testPreferences() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        SharedPreferences preferences = appContext.getSharedPreferences(TEST_CONFIG_NAME, Context.MODE_PRIVATE);
        preferences.edit().clear().commit();//reset empty

        TestConfig config = ConfigManager.getInstance().getConfig(TestConfig.class);

        assertEquals(config.name().get(), "");
        assertEquals(config.name().get("empty"), "empty");
        assertTrue(config.name().isEmpty());
        assertFalse(config.name().isPresent());
        config.name().set("Alice");
        assertEquals(config.name().get(), "Alice");
        assertEquals(config.name().get("empty"), "Alice");
        assertEquals(preferences.getString("name", ""), "Alice");

        config.nameWithAnno();
        assertEquals(config.nameWithAnno().get(), "");
        assertEquals(config.nameWithAnno().get("empty"), "empty");
        assertTrue(config.nameWithAnno().isEmpty());
        assertFalse(config.nameWithAnno().isPresent());
        config.nameWithAnno().set("Akane");
        assertEquals(config.nameWithAnno().get(), "Akane");
        assertEquals(config.nameWithAnno().get("empty"), "Akane");
        assertEquals(preferences.getString("name_with_annotation", ""), "Akane");
        assertThrows(RuntimeException.class, () -> config.nameWithAnno().set(""));

        assertEquals(config.sexText().get(), "未知");
        assertEquals(config.sexText().get("empty"), "empty");
        assertTrue(config.sexText().isEmpty());
        assertFalse(config.sexText().isPresent());
        config.sexText().set("男");
        assertEquals(config.sexText().get(), "男");
        assertEquals(config.sexText().get("empty"), "男");
        assertEquals(preferences.getString("sex_text", ""), "男");
        assertThrows(RuntimeException.class, () -> config.sexText().set("male"));

        assertEquals(config.age().get(), (Integer) 0);
        assertEquals(config.age().get(-1), (Integer) (-1));
        assertTrue(config.age().isEmpty());
        assertFalse(config.age().isPresent());
        config.age().set(2);
        assertEquals(config.age().get(), (Integer) 2);
        assertEquals(config.age().get(-1), (Integer) 2);
        assertEquals(preferences.getInt("age", -1), 2);

        assertEquals(config.ageWithLimit().get(), (Integer) 1);
        assertEquals(config.ageWithLimit().get(-1), (Integer) (-1));
        assertTrue(config.ageWithLimit().isEmpty());
        assertFalse(config.ageWithLimit().isPresent());
        config.ageWithLimit().set(2);
        assertEquals(config.ageWithLimit().get(), (Integer) 2);
        assertEquals(config.ageWithLimit().get(-1), (Integer) 2);
        assertEquals(preferences.getInt("age_limited", -1), 2);
        assertThrows(RuntimeException.class, () -> config.ageWithLimit().set(300));

        assertEquals(config.sexNumber().get(), (Integer) 0);
        assertEquals(config.sexNumber().get(-1), (Integer) (-1));
        assertTrue(config.sexNumber().isEmpty());
        assertFalse(config.sexNumber().isPresent());
        config.sexNumber().set(2);
        assertEquals(config.sexNumber().get(), (Integer) 2);
        assertEquals(config.sexNumber().get(-1), (Integer) 2);
        assertEquals(preferences.getInt("sex_number", -1), 2);
        assertThrows(RuntimeException.class, () -> config.sexNumber().set(300));

        assertEquals(config.lastUpdateTime().get(), (Long) 0L);
        assertEquals(config.lastUpdateTime().get(-1L), (Long) (-1L));
        assertTrue(config.lastUpdateTime().isEmpty());
        assertFalse(config.lastUpdateTime().isPresent());
        config.lastUpdateTime().set(2L);
        assertEquals(config.lastUpdateTime().get(), (Long) 2L);
        assertEquals(config.lastUpdateTime().get(-1L), (Long) 2L);
        assertEquals(preferences.getLong("last_update_time", -1L), 2L);
        assertThrows(RuntimeException.class, () -> config.lastUpdateTime().set(-100L));

        assertEquals(config.longLimited().get(), (Long) 1L);
        assertEquals(config.longLimited().get(-1L), (Long) (-1L));
        assertTrue(config.longLimited().isEmpty());
        assertFalse(config.longLimited().isPresent());
        config.longLimited().set(2L);
        assertEquals(config.longLimited().get(), (Long) 2L);
        assertEquals(config.longLimited().get(-1L), (Long) 2L);
        assertEquals(preferences.getLong("longLimited", -1L), 2L);
        assertThrows(RuntimeException.class, () -> config.longLimited().set(5L));

        assertEquals(config.height().get(), (Float) 0F);
        assertEquals(config.height().get(-1F), (Float) (-1F));
        assertTrue(config.height().isEmpty());
        assertFalse(config.height().isPresent());
        config.height().set(2F);
        assertEquals(config.height().get(), (Float) 2F);
        assertEquals(config.height().get(-1F), (Float) 2F);
        assertEquals(preferences.getFloat("height", -1F), 2F, 0.001F);

        assertEquals(config.heightLimited().get(), (Float) 0F);
        assertEquals(config.heightLimited().get(-1F), (Float) (-1F));
        assertTrue(config.heightLimited().isEmpty());
        assertFalse(config.heightLimited().isPresent());
        config.heightLimited().set(2F);
        assertEquals(config.heightLimited().get(), (Float) 2F);
        assertEquals(config.heightLimited().get(-1F), (Float) 2F);
        assertEquals(preferences.getFloat("height_limited", -1F), 2F, 0.001F);
        assertThrows(RuntimeException.class, () -> config.heightLimited().set(100F));

        assertEquals(config.floatLimited().get(), (Float) 0F);
        assertEquals(config.floatLimited().get(-1F), (Float) (-1F));
        assertTrue(config.floatLimited().isEmpty());
        assertFalse(config.floatLimited().isPresent());
        config.floatLimited().set(2F);
        assertEquals(config.floatLimited().get(), (Float) 2F);
        assertEquals(config.floatLimited().get(-1F), (Float) 2F);
        assertEquals(preferences.getFloat("floatLimited", -1F), 2F, 0.001F);
        assertThrows(RuntimeException.class, () -> config.floatLimited().set(100F));

        assertEquals(config.alive().get(), false);
        assertEquals(config.alive().get(true), true);
        assertTrue(config.alive().isEmpty());
        assertFalse(config.alive().isPresent());
        config.alive().set(true);
        assertEquals(config.alive().get(), true);
        assertEquals(config.alive().get(false), true);
        assertTrue(preferences.getBoolean("alive", true));

        assertEquals(config.aliveLimited().get(), true);
        assertEquals(config.aliveLimited().get(false), false);
        assertTrue(config.aliveLimited().isEmpty());
        assertFalse(config.aliveLimited().isPresent());
        config.aliveLimited().set(true);
        assertEquals(config.aliveLimited().get(), true);
        assertEquals(config.aliveLimited().get(false), true);
        assertTrue(preferences.getBoolean("alive_limited", true));

        assertArrayEquals(config.friends().get().toArray(), new String[0]);
        assertEquals(config.friends().get(Collections.emptySet()), Collections.emptySet());
        assertTrue(config.friends().isEmpty());
        assertFalse(config.friends().isPresent());
        config.friends().set(new HashSet<>(Arrays.asList("A", "B", "C")));
        assertArrayEquals(config.friends().get().toArray(), new String[]{"A", "B", "C"});
        assertArrayEquals(config.friends().get(Collections.emptySet()).toArray(), new String[]{"A", "B", "C"});
        assertArrayEquals(preferences.getStringSet("friends", Collections.emptySet()).toArray(), new String[]{"A", "B", "C"});

        assertArrayEquals(config.useComputerOsSet().get().toArray(), new String[0]);
        assertEquals(config.useComputerOsSet().get(Collections.emptySet()), Collections.emptySet());
        assertTrue(config.useComputerOsSet().isEmpty());
        assertFalse(config.useComputerOsSet().isPresent());
        config.useComputerOsSet().set(new HashSet<>(Arrays.asList("linux", "windows")));
        assertArrayEquals(config.useComputerOsSet().get().stream().sorted().toArray(), new String[]{"linux", "windows"});
        assertArrayEquals(config.useComputerOsSet().get(Collections.emptySet()).stream().sorted().toArray(), new String[]{"linux", "windows"});
        assertArrayEquals(preferences.getStringSet("use_computer_os_set", Collections.emptySet()).stream().sorted().toArray(), new String[]{"linux", "windows"});
        assertThrows(RuntimeException.class, () -> config.useComputerOsSet().set(new HashSet<>(Arrays.asList("linux", "xxx"))));
    }
}

@Keep
@Config.Name(ExampleInstrumentedTest.TEST_CONFIG_NAME)
interface TestConfig extends Config {

    Property<String> name();

    @Config.StringItem(key = "name_with_annotation", supportEmpty = false)
    Property<String> nameWithAnno();

    @Config.StringItem(key = "sex_text", supportEmpty = false, valueOf = {"男", "女", "未知"}, defaultValue = "未知")
    Property<String> sexText();

    Property<Integer> age();

    @Config.IntItem(key = "age_limited", start = 1, to = 200, defaultValue = 1)
    Property<Integer> ageWithLimit();

    @Config.IntItem(key = "sex_number", valueOf = {0, 1, 2})
    Property<Integer> sexNumber();

    @Config.LongItem(key = "last_update_time", start = 0L, defaultValue = 0L)
    Property<Long> lastUpdateTime();

    @Config.LongItem(valueOf = {1L, 2L, 3L}, defaultValue = 1L)
    Property<Long> longLimited();

    Property<Float> height();

    @Config.FloatItem(key = "height_limited", start = 0F, to = 5F)
    Property<Float> heightLimited();

    @Config.FloatItem(valueOf = {1.0f, 2.0f, 3.0f})
    Property<Float> floatLimited();

    Property<Boolean> alive();

    @BooleanItem(key = "alive_limited", defaultValue = true)
    Property<Boolean> aliveLimited();

    Property<Set<String>> friends();

    @Config.StringSetItem(key = "use_computer_os_set", valueOf = {"windows", "mac", "ios", "android", "linux"})
    Property<Set<String>> useComputerOsSet();
}