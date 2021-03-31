package org.sfvl.doctesting;

import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

public class DocTestingDocumentation extends DocumentationBuilder {

    public DocTestingDocumentation() {
        super("Document testing tool");
        withClassesToInclude(getClassesToDocument());
        withLocation(DocTestingDocumentation.class.getPackage());
        withOptionAdded("source-highlighter", "rouge");
        withOptionAdded("toclevels", "4");
        withStructureBuilder(DocTestingDocumentation.class,
                b -> b.getDocumentOptions(),
                b -> String.format("= %s\n", b.documentationTitle),
                b -> this.generalInformation(b.formatter),
                b -> b.includeClasses(),
                b -> this.getStyle()
        );
    }

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
        return new ClassFinder().testClasses(DocTestingDocumentation.class.getPackage(),
                this::toBeInclude);
    }

    public static void main(String... args) throws IOException {
        Document.produce(new DocTestingDocumentation());
    }
}
