package com.nagi.preferencedemo;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.nagi.neopreference.Config;
import com.nagi.neopreference.ConfigManager;
import com.nagi.neopreference.Property;
import com.nagi.preferencedemo.databinding.ActivityAutoConfigBinding;
import com.nagi.preferencedemo.databinding.DialogInputBinding;
import com.nagi.preferencedemo.databinding.HolderConfigPropertyBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class AutoConfigActivity extends AppCompatActivity {
    public static final String ARG_CONFIG_CLASS = "config_class";
    private static final int OBJECT_TYPE = 0;
    private static final int INTEGER_TYPE = 1;
    private static final int FLOAT_TYPE = 2;
    private static final int STRING_TYPE = 3;
    private static final int BOOLEAN_TYPE = 4;
    private static final int LONG_TYPE = 5;

    public static void start(Activity activity, Class<?> configClass) {
        Intent intent = new Intent(activity, AutoConfigActivity.class);
        intent.putExtra(ARG_CONFIG_CLASS, configClass);
        activity.startActivity(intent);
    }

    private final List<Property<?>> propertyList = new ArrayList<>();


    private final RecyclerView.Adapter<ConfigItemHolder> adapter = new RecyclerView.Adapter<>() {
        @NonNull
        @Override
        public ConfigItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case INTEGER_TYPE:
                    return new IntegerItemHolder(parent);
                case FLOAT_TYPE:
                    return new FloatItemHolder(parent);
                case LONG_TYPE:
                    return new LongItemHolder(parent);
                case BOOLEAN_TYPE:
                    return new BooleanItemHolder(parent);
                case STRING_TYPE:
                    return new StringItemHolder(parent);
                case OBJECT_TYPE:
                    return new ObjectItemHolder(parent);
                default:
                    return null;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ConfigItemHolder holder, int position) {
            holder.setData(propertyList.get(position));
        }

        @Override
        public int getItemCount() {
            return propertyList.size();
        }

        @Override
        public int getItemViewType(int position) {
            Class<?> valueClass = propertyList.get(position).getValueClass();
            if (valueClass.equals(Integer.class)) {
                return INTEGER_TYPE;
            } else if (valueClass.equals(Float.class)) {
                return FLOAT_TYPE;
            } else if (valueClass.equals(Long.class)) {
                return LONG_TYPE;
            } else if (valueClass.equals(Boolean.class)) {
                return BOOLEAN_TYPE;
            } else if (valueClass.equals(String.class)) {
                return STRING_TYPE;
            } else {
                return OBJECT_TYPE;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAutoConfigBinding binding = ActivityAutoConfigBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.rvConfigList.setHasFixedSize(true);
        binding.rvConfigList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        binding.rvConfigList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvConfigList.setAdapter(adapter);

        Class<? extends Config> configClass = (Class<? extends Config>) getIntent().getSerializableExtra(ARG_CONFIG_CLASS);
        Config config = ConfigManager.getInstance().getConfig(configClass);
        propertyList.addAll(config.getAll());
        adapter.notifyItemRangeInserted(0, propertyList.size());

        for (int i = 0; i < propertyList.size(); i++) {
            int index = i;
            propertyList.get(i).addListener(this, s -> adapter.notifyItemChanged(index));
        }
    }

    static abstract class ConfigItemHolder<T> extends RecyclerView.ViewHolder {
        final HolderConfigPropertyBinding binding;

        public ConfigItemHolder(@NonNull HolderConfigPropertyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(Property<T> property) {
            if (TextUtils.isEmpty(property.getDescription())) {
                binding.tvPropertyName.setText(property.getKey());
            } else {
                binding.tvPropertyName.setText(property.getKey() + "(" + property.getDescription() + ")");
            }

            binding.tvPropertyValue.setText(property.getValueString());
        }
    }

    static class IntegerItemHolder extends ConfigItemHolder<Integer> {

        public IntegerItemHolder(@NonNull ViewGroup parent) {
            super(HolderConfigPropertyBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        void setData(Property<Integer> property) {
            super.setData(property);

            binding.btnEdit.setOnClickListener(v -> {
                DialogInputBinding dialogBinding = DialogInputBinding.inflate(LayoutInflater.from(itemView.getContext()));
                AlertDialog alertDialog = new AlertDialog.Builder(itemView.getContext())
                        .setTitle("Set " + property.getKey())
                        .setView(dialogBinding.getRoot())
                        .setPositiveButton("save", (dialog, which) -> property.set(Integer.parseInt(dialogBinding.etInput.getText().toString())))
                        .create();
                alertDialog.show();

                Button button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                dialogBinding.etInput.setHint("Please input a integer");
                dialogBinding.etInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                dialogBinding.etInput.addTextChangedListener(onTextChanged(s -> button.setEnabled(!TextUtils.isEmpty(s))));
                dialogBinding.etInput.setText(property.exists() ? String.valueOf(property.get()) : "");
            });
        }
    }

    static class FloatItemHolder extends ConfigItemHolder<Float> {

        public FloatItemHolder(@NonNull ViewGroup parent) {
            super(HolderConfigPropertyBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        void setData(Property<Float> property) {
            super.setData(property);
            binding.btnEdit.setOnClickListener(v -> {
                DialogInputBinding dialogBinding = DialogInputBinding.inflate(LayoutInflater.from(itemView.getContext()));
                AlertDialog alertDialog = new AlertDialog.Builder(itemView.getContext())
                        .setTitle("Set " + property.getKey())
                        .setView(dialogBinding.getRoot())
                        .setPositiveButton("save", (dialog, which) -> property.set(Float.parseFloat(dialogBinding.etInput.getText().toString())))
                        .create();
                alertDialog.show();

                Button button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                dialogBinding.etInput.setHint("Please input a float");
                dialogBinding.etInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                dialogBinding.etInput.addTextChangedListener(onTextChanged(s -> button.setEnabled(!TextUtils.isEmpty(s))));
                dialogBinding.etInput.setText(property.exists() ? String.valueOf(property.get()) : "");
            });
        }
    }

    static class BooleanItemHolder extends ConfigItemHolder<Boolean> {

        public BooleanItemHolder(@NonNull ViewGroup parent) {
            super(HolderConfigPropertyBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        void setData(Property<Boolean> property) {
            super.setData(property);
            binding.btnEdit.setOnClickListener(v -> {
                AtomicBoolean value = new AtomicBoolean(property.get(false));
                AlertDialog alertDialog = new AlertDialog.Builder(itemView.getContext())
                        .setTitle("Set " + property.getKey())
                        .setSingleChoiceItems(new CharSequence[]{"true", "false"}, value.get() ? 0 : 1, (dialog, which) -> value.set(which == 0))
                        .setPositiveButton("save", (dialog, which) -> property.set(value.get()))
                        .create();
                alertDialog.show();
            });
        }
    }

    static class LongItemHolder extends ConfigItemHolder<Long> {

        public LongItemHolder(@NonNull ViewGroup parent) {
            super(HolderConfigPropertyBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        void setData(Property<Long> property) {
            super.setData(property);
            binding.btnEdit.setOnClickListener(v -> {
                DialogInputBinding dialogBinding = DialogInputBinding.inflate(LayoutInflater.from(itemView.getContext()));
                AlertDialog alertDialog = new AlertDialog.Builder(itemView.getContext())
                        .setTitle("Set " + property.getKey())
                        .setView(dialogBinding.getRoot())
                        .setPositiveButton("save", (dialog, which) -> property.set(Long.parseLong(dialogBinding.etInput.getText().toString())))
                        .create();
                alertDialog.show();

                Button button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                dialogBinding.etInput.setHint("Please input a long");
                dialogBinding.etInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                dialogBinding.etInput.addTextChangedListener(onTextChanged(s -> button.setEnabled(!TextUtils.isEmpty(s))));
                dialogBinding.etInput.setText(property.exists() ? String.valueOf(property.get()) : "");
            });
        }
    }

    static class StringItemHolder extends ConfigItemHolder<String> {

        public StringItemHolder(@NonNull ViewGroup parent) {
            super(HolderConfigPropertyBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        void setData(Property<String> property) {
            super.setData(property);
            binding.btnEdit.setOnClickListener(v -> {
                DialogInputBinding dialogBinding = DialogInputBinding.inflate(LayoutInflater.from(itemView.getContext()));
                AlertDialog alertDialog = new AlertDialog.Builder(itemView.getContext())
                        .setTitle("Set " + property.getKey())
                        .setView(dialogBinding.getRoot())
                        .setPositiveButton("save", (dialog, which) -> property.set(dialogBinding.etInput.getText().toString()))
                        .create();
                alertDialog.show();

                Button button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                dialogBinding.etInput.setHint("Please input a string");
                dialogBinding.etInput.addTextChangedListener(onTextChanged(s -> button.setEnabled(!TextUtils.isEmpty(s))));
                dialogBinding.etInput.setText(property.exists() ? String.valueOf(property.get()) : "");
            });
        }
    }

    static class ObjectItemHolder extends ConfigItemHolder<Object> {

        public ObjectItemHolder(@NonNull ViewGroup parent) {
            super(HolderConfigPropertyBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        void setData(Property<Object> property) {
            super.setData(property);
            binding.btnEdit.setVisibility(View.GONE);
        }
    }

    static TextWatcher onTextChanged(Consumer<CharSequence> listener) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                listener.accept(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
    }
}