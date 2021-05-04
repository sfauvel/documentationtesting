package org.sfvl.doctesting.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This object is used to store the text that need to be write to the final document.
 */
public class DocWriter {
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface NoTitle {
    }

    private StringBuffer sb = new StringBuffer();

    /**
     * Write a text to the output.
     */
    public void write(String... texts) {
        sb.append(Arrays.stream(texts).collect(Collectors.joining("\n")));
    }

    public String read() {
        return sb.toString();
    }

    public String formatOutput(String displayName, Method testMethod) {
        boolean isTitle = testMethod.getAnnotation(NoTitle.class) == null;
        String title = isTitle
                ? String.join("",
                String.format("[#%s]", titleId(testMethod)),
                "\n",
                "= " + formatTitle(displayName, testMethod) + "\n",
                "\n")
                : "";
        return String.join("",
                title,
                CodeExtractor.getComment(testMethod).map(comment -> comment + "\n\n").orElse(""),
                read());
    }

    public String titleId(Method testMethod) {
        return String.format("%s_%s",
                testMethod.getDeclaringClass().getName()
                        .replace(".", "_")
                        .replace("$", "_"),
                testMethod.getName());
    }

    /**
     * Return the name to use as title from a test method.
     * It returns the value specified with _DisplayName_ annotation.
     * If annotation is not present, this is the method name that will be returned
     * after some test formatting (remove '_', uppercase first letter).
     *
     * It's based on value return by _displayName_ method.
     * It returns either DisplayName annotation value or method name.
     *
     * @param displayName
     * @param method
     * @return
     */
    public String formatTitle(String displayName, Method method) {
        final String parameters = Arrays.stream(method.getParameterTypes())
                .map(Class::getSimpleName)
                .collect(Collectors.joining(","));
        String methodName = method.getName();
        if (displayName.equals(methodName + "(" + parameters + ")")) {
            String title = methodName.replace("_", " ");
            return title.substring(0, 1).toUpperCase() + title.substring(1);
        } else {
            return displayName;
        }
    }

    public void reset() {
        sb = new StringBuffer();
    }
}
