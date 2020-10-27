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

    @Override
    protected Set<Method> getAnnotatedMethod(Class<? extends Annotation> annotation, String packageToScan) {
        return super.getAnnotatedMethod(annotation, packageToScan).stream()
                .filter(m -> !(m.isAnnotationPresent(NotIncludeToDoc.class)
                        || m.getDeclaringClass().isAnnotationPresent(NotIncludeToDoc.class)))
                .collect(Collectors.toSet());
    }


    public static void main(String... args) throws IOException {
        final DocTestingDocumentation generator = new DocTestingDocumentation();

        generator.generate();
    }

}
