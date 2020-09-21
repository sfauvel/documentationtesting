package org.sfvl.demo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TestCategory {
    public static enum Cat {
        Simple,
        Long,
        Many
    }
    Cat category();
}
