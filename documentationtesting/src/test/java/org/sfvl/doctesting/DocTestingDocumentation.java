package org.sfvl.doctesting;

import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.utils.ClassFinder;
import org.sfvl.doctesting.utils.CodeExtractor;
import org.sfvl.doctesting.utils.DocWriter;
import org.sfvl.doctesting.utils.DocumentationNamer;
import org.sfvl.doctesting.writer.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;

public class DocTestingDocumentation implements DocumentProducer {

    protected final Formatter formatter = new AsciidocFormatter();

    public String build() {
        return formatter.paragraphSuite(
                getOptions(),
                formatter.title(1, "Document testing tool"),
                generalInformation(formatter),
                includeClasses(formatter),
                getStyle()
                );
    }

    public String getOptions() {
        return formatter.paragraph(
                new Options(formatter).withCode().trim(),
                new Option("toclevels", "4").format()
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

    public String includeClasses(Formatter formatter) {
        final Path location = DocumentationNamer.toPath(DocTestingDocumentation.class.getPackage());
        return new Classes(formatter).includeClasses(location, getClassesToDocument());
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

    @Override
    public void produce() throws IOException {
        new Document(this.build()).saveAs(this.getClass());
    }

    public static void main(String... args) throws IOException {
        new DocTestingDocumentation().produce();
    }
}
