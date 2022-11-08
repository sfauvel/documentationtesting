package org.sfvl.doctesting.writer;

import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.utils.NoTitle;

import java.lang.reflect.Method;
import java.util.function.BiFunction;

class MethodDocumentation<F extends Formatter> {
    private F formatter;
    private BiFunction<String, Method, String> titleFormatter;

    public MethodDocumentation(F formatter, BiFunction<String, Method, String> titleFormatter) {
        this.formatter = formatter;
        this.titleFormatter = titleFormatter;
    }

    public String format(String title, Method method, String content, String docPath, String comment) {
        return String.join("",
                docPath,
                "\n\n",
                formatAdocTitle(title, method),
                comment,
                content);
    }

    private String formatAdocTitle(String title, Method testMethod) {
        boolean isTitle = testMethod.getAnnotation(NoTitle.class) == null;

        return isTitle
                ? formatter.paragraph(
                formatter.blockId(titleId(testMethod)),
                formatter.title(1, titleFormatter.apply(title, testMethod)).trim()
        ) : "";
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
