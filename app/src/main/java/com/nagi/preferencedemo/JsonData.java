package com.nagi.preferencedemo;

public interface JsonData {

    @interface JsonItem {
        String key();

        String description() default "";
    }
}
