package org.sfvl.doctesting.writer;

import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.utils.NoTitle;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.BiFunction;

class MethodDocumentation<F extends Formatter> {
    private F formatter;
    private BiFunction<Class, Method, Optional<String>> comment_extractor;

    public MethodDocumentation(F formatter, BiFunction<Class, Method, Optional<String>> comment) {
        this.formatter = formatter;
        this.comment_extractor = comment;
    }

    public String format(String title, Method method, String content) {
        return String.join("",
                formatAdocTitle(title, method),
                comment_extractor.apply(method.getDeclaringClass(), method).map(c -> c + "\n\n").orElse(""),
                content);
    }

    private String formatAdocTitle(String title, Method testMethod) {
        boolean isTitle = testMethod.getAnnotation(NoTitle.class) == null;

        return isTitle
                ? formatter.paragraph(
                formatter.blockId(titleId(testMethod)),
                getTitle(title)
        ) : "";
    }

    protected String getTitle(String title) {
        return formatter.title(1, title).trim();
    }

    public String titleId(Method testMethod) {
        return String.format("%s_%s",
                titleId(testMethod.getDeclaringClass()),
                testMethod.getName()).toLowerCase();
    }

    public String titleId(Class clazz) {
        return clazz.getName()
                .replace(".", "_")
                .replace("$", "_")
                .toLowerCase();
    }

}
