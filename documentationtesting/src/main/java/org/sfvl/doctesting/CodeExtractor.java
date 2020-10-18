package org.sfvl.doctesting;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaType;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CodeExtractor {
    static JavaProjectBuilder builder;

    /**
     * Init JavaProjectBuilder it's a bit long (more than 100ms).
     * We init it only once to avoid this low performances.
     */
    static {
        if (builder == null) {
            builder = new JavaProjectBuilder();
            builder.addSourceTree(new File("src/main/java"));
            builder.addSourceTree(new File("src/test/java"));
        }
    }

    public static JavaProjectBuilder getBuilder() {
        return builder;
    }

    public static String getComment(Class<?> clazz) {
        JavaClass javaClass = builder.getClassByName(clazz.getCanonicalName());

        return Optional.ofNullable(javaClass.getComment()).orElse("");
    }

    public static String getComment(Class<?> clazz, String methodName) {
        return getComment(clazz, methodName, Collections.emptyList());
    }

    public static String getComment(Class<?> clazz, String methodName, List<JavaType> argumentList) {
        JavaClass javaClass = builder.getClassByName(clazz.getCanonicalName());

        JavaMethod method = javaClass.getMethod(methodName, argumentList, false);
        while (method == null && javaClass.getSuperJavaClass() != null) {
            javaClass = javaClass.getSuperJavaClass();
            method = javaClass.getMethod(methodName, argumentList, false);
        }
        return Optional.ofNullable(method).map(c -> c.getComment()).orElse("");
    }
}
