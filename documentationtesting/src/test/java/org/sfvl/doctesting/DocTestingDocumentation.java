package org.sfvl.doctesting;

import org.sfvl.doctesting.junitextension.ApprovalsExtension;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

public class DocTestingDocumentation extends MainDocumentation {

    @Override
    protected String getHeader() {
        String style = String.join("\n", "++++",
                "<style>",
                "#content {",
                "   max-width: unset;",
                "   padding-left: 5%;",
                "   padding-right: 5%;",
                "}",
                "</style>",
                "++++");

        return formatter.paragraphSuite(
                ":source-highlighter: rouge\n" + getDocumentOptions() + "\n:toclevels: 4",
                style,
                "= Document testing tool",
                generalInformation());
    }

    @Override
    protected String generalInformation() {
        return formatter.paragraphSuite(super.generalInformation(),
                "This document describes usage of classes to create test from generated documentation.",
                "* <<" + ApprovalsExtension.class.getSimpleName() + ">>: JUnit extension to check document.",
                "* <<" + DocWriter.class.getSimpleName() + ">>: Store document before writting it.",
                "* <<" + CodeExtractor.class.getSimpleName() + ">>: Help to extract information from code.");
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
