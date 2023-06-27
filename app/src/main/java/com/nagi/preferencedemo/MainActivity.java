package com.nagi.preferencedemo;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.nagi.neopreference.ConfigManager;
import com.nagi.preferencedemo.databinding.ActivityMainBinding;

import java.util.Arrays;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DemoConfig config = ConfigManager.getInstance().getConfig(DemoConfig.class);
        binding.etFloat.setText(String.valueOf(config.floatProperty().get()));
        binding.etInt.setText(String.valueOf(config.intProperty().get()));
        binding.etLong.setText(String.valueOf(config.longProperty().get()));
        binding.switchBool.setChecked(config.boolProperty().get());
        binding.etString.setText(String.valueOf(config.stringProperty().get()));
        binding.etStringSet.setText(config.stringSetProperty().get().stream()
                .reduce((s1, s2) -> s1 + " " + s2).orElse(""));

        binding.btnSave.setOnClickListener(v -> {
            config.intProperty().set(Integer.parseInt(String.valueOf(binding.etInt.getText())));
            config.floatProperty().set(Float.parseFloat(String.valueOf(binding.etFloat.getText())));
            config.longProperty().set(Long.parseLong(String.valueOf(binding.etLong.getText())));
            config.stringProperty().set(String.valueOf(binding.etString.getText()));
            config.boolProperty().set(binding.switchBool.isChecked());
            config.stringSetProperty().set(Arrays.stream(String.valueOf(binding.etStringSet.getText())
                    .split(" ")).collect(Collectors.toSet()));
        });

        ConfigManager.getInstance().addListener(this, DemoConfig.NAME, (key, value) -> {
            Log.i("SPECTRE", "MainActivity.onCreate preference content change => " + key + " - " + value);//TODO delete
        });

        config.intProperty().addListener(this, newValue -> Log.i("SPECTRE", "int property updated:" + newValue + "\n"));
        config.floatProperty().addListener(this, newValue -> Log.i("SPECTRE", "float property updated:" + newValue + "\n"));
        config.longProperty().addListener(this, newValue -> Log.i("SPECTRE", "long property updated:" + newValue + "\n"));
        config.boolProperty().addListener(this, newValue -> Log.i("SPECTRE", "bool property updated:" + newValue + "\n"));
        config.stringProperty().addListener(this, newValue -> Log.i("SPECTRE", "string property updated:" + newValue + "\n"));
        config.stringSetProperty().addListener(this, newValue -> Log.i("SPECTRE", "string set property updated:" + newValue.stream().reduce((s1, s2) -> s1 + ", " + s2) + "\n"));
    }
}