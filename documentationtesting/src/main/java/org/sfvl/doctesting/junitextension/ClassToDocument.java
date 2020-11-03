package org.sfvl.doctesting.junitextension;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ClassToDocument {
    Class<?> clazz();
}