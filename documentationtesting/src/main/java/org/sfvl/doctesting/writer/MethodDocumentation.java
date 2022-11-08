package org.sfvl.doctesting.writer;

import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.utils.NoTitle;

import java.lang.reflect.Method;
import java.util.function.BiFunction;

class MethodDocumentation<F extends Formatter> {
    private F formatter;

    public MethodDocumentation(F formatter, BiFunction<String, Method, String> titleFormatter) {
        this.formatter = formatter;
    }

    public String format(String title, Method method, String content, String comment) {
        return String.join("",
                formatAdocTitle(title, method),
                comment,
                content);
    }

    private String formatAdocTitle(String title, Method testMethod) {
        boolean isTitle = testMethod.getAnnotation(NoTitle.class) == null;

        return isTitle
                ? formatter.paragraph(
                formatter.blockId(titleId(testMethod)),
                getTitle(testMethod, title)
        ) : "";
    }

    protected String getTitle(Method testMethod, String title) {
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
