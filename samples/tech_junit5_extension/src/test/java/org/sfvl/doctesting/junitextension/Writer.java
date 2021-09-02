package org.sfvl.doctesting.junitextension;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

public class Writer {
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
        return String.join("\n\n",
                "= " + formatTitle(displayName, testMethod.getName()),
                getComment(testMethod),
                read());
    }

    /**
     * Return name specified in DisplayName annotation.
     * If annotation is not present, this is the method name that will be returned
     * after some test formatting (remove '_', uppercase first letter).
     *
     * @param displayName
     * @param methodName
     * @return
     */
    private String formatTitle(String displayName, String methodName) {
        if (displayName.equals(methodName + "()")) {
            String title = methodName.replace("_", " ");
            return title.substring(0, 1).toUpperCase() + title.substring(1);
        } else {
            return displayName;
        }
    }

    private String getComment(Method testMethod) {
        JavaProjectBuilder builder = new JavaProjectBuilder();
        builder.addSourceTree(new File("src/test/java"));

        JavaClass javaClass = builder.getClassByName(testMethod.getDeclaringClass().getCanonicalName());

        JavaMethod method = javaClass.getMethod(testMethod.getName(), Collections.emptyList(), false);
        return Optional.ofNullable(method.getComment()).orElse("");
    }

}
