package org.sfvl.doctesting;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

public class DocTestingDocumentation extends MainDocumentation {

    @Override
    protected String getHeader() {
        return ":source-highlighter: rouge\n" +
                getDocumentOptions() +
                "= " + DOCUMENTATION_TITLE + "\n\n" +
                generalInformation();
    }

    public boolean toBeInclude(Class<?> clazz) {
        if (clazz == null) {
            return true;
        }
        return !clazz.isAnnotationPresent(NotIncludeToDoc.class)
                && toBeInclude(clazz.getDeclaringClass());

    }
    public boolean toBeInclude(Method method) {
        return !method.isAnnotationPresent(NotIncludeToDoc.class)
            && toBeInclude(method.getDeclaringClass());
    }

    @Override
    protected Set<Method> getAnnotatedMethod(Class<? extends Annotation> annotation, String packageToScan) {
        return super.getAnnotatedMethod(annotation, packageToScan).stream()
                .filter(this::toBeInclude)
                .collect(Collectors.toSet());
    }


    public static void main(String... args) throws IOException {
        final DocTestingDocumentation generator = new DocTestingDocumentation();

        generator.generate();
    }

}
