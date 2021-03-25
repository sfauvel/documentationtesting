package org.sfvl.doctesting;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DocTestingDocumentation {
    final PathProvider pathProvider = new PathProvider();

    private String getStyle() {
        String style = String.join("\n", "++++",
                "<style>",
                "#content {",
                "   max-width: unset;",
                "   padding-left: 5%;",
                "   padding-right: 5%;",
                "}",
                "</style>",
                "++++");
        return style;
    }

    protected String generalInformation(Formatter formatter) {
        return formatter.paragraphSuite(
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

    private List<Class<?>> getClassesToDocument() {
        final String prefix = DocumentationNamer.toPath(DocTestingDocumentation.class.getPackage()).toString();
        Reflections reflections = new Reflections(prefix, new MethodAnnotationsScanner());

        final Stream<Method> methodsAnnotatedWith = reflections.getMethodsAnnotatedWith(Test.class).stream()
                .filter(this::toBeInclude);

        return methodsAnnotatedWith
                .map(method -> CodeExtractor.getFirstEnclosingClassBefore(method, null))
                .distinct()
                .sorted(Comparator.comparing(Class::getName))
                .collect(Collectors.toList());
    }

    public void writeToFile(String document, Path docPath) throws IOException {
        final Path fullPath = pathProvider.getProjectPath().resolve(Paths.get("src", "test", "docs")).resolve(docPath);
        try (FileWriter file = new FileWriter(fullPath.toFile())) {

            file.write(document);
        }
    }

    public void generate() throws IOException {
        String document = new DocumentationBuilder("Document testing tool")
                .withClassesToInclude(getClassesToDocument())
                .withLocation(DocTestingDocumentation.class.getPackage())
                .withOptionAdded("source-highlighter", "rouge")
                .withOptionAdded("toclevels", "4")
                .withStructure(
                        b -> b.getDocumentOptions(),
                        b -> String.format("= %s\n", b.documentationTitle),
                        b -> this.generalInformation(b.formatter),
                        b -> b.includeClasses(),
                        b -> this.getStyle()
                )
                .build();

        writeToFile(document, DocumentationNamer.toPath(DocTestingDocumentation.class, "", ".adoc"));
    }

    public static void main(String... args) throws IOException {
        final DocTestingDocumentation generator = new DocTestingDocumentation();

        generator.generate();
    }
}
