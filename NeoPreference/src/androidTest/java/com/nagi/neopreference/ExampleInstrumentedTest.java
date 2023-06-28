package com.nagi.neopreference;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.Keep;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

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

    @Config.LongItem(valueOf = {1L, 2L, 3L})
    Property<Long> longLimited();

    Property<Float> height();

    @Config.FloatItem(key = "height_limited", start = 0F, to = 5F)
    Property<Float> heightLimited();

    @Config.FloatItem(valueOf = {1.0f, 2.0f, 3.0f})
    Property<Float> floatLimited();

    Property<Boolean> alive();

    Property<Set<String>> friends();

    @Config.StringSetItem(key = "use_computer_os_set", valueOf = {"windows", "mac", "ios", "android", "linux"})
    Property<Set<String>> useComputerOsSet();
}