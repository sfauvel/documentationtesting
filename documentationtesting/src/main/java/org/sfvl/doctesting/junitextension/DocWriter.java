package org.sfvl.doctesting.junitextension;

import org.sfvl.doctesting.CodeExtractor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This object is used to store the text that need to be write to the final document.
 */
public class DocWriter {
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
        return  String.join("",
                "= " + formatTitle(displayName, testMethod.getName()) + "\n\n",
                CodeExtractor.getComment(testMethod).map(comment -> comment + "\n\n").orElse(""),
                read());
    }

    /**
     * Return name specified in DisplayName annotation.
     * If annotation is not present, this is the method name that will be returned
     * after some test formatting (remove '_', uppercase first letter).
     * @param displayName
     * @param methodName
     * @return
     */
    private String formatTitle(String displayName, String methodName) {
        if (displayName.equals(methodName+"()")) {
            String title = methodName.replace("_", " ");
            return title.substring(0, 1).toUpperCase() + title.substring(1);
        } else {
            return displayName;
        }
    }

}
