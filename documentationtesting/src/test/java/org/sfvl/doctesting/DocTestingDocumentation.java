package org.sfvl.doctesting;

import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.utils.ClassFinder;
import org.sfvl.doctesting.utils.CodeExtractor;
import org.sfvl.doctesting.utils.DocWriter;
import org.sfvl.doctesting.utils.DocumentationNamer;
import org.sfvl.doctesting.writer.Document;
import org.sfvl.doctesting.writer.DocumentProducer;
import org.sfvl.doctesting.writer.Options;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class DocTestingDocumentation implements DocumentProducer {

    protected final Formatter formatter = new AsciidocFormatter();

    public String build() {
        return formatter.paragraphSuite(
                new Options(formatter).withCode(),
                formatter.title(1, "Document testing tool"),
                generalInformation(formatter),
                includeClasses(),
                getStyle()
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

    public String includeClasses() {
        final Path location = DocumentationNamer.toPath(DocTestingDocumentation.class.getPackage());
        return includeClasses(location, getClassesToDocument());
    }

    private String includeClasses(Path location, List<Class<?>> classesToInclude) {

        return classesToInclude.stream()
                .map(c -> getRelativeFilePath(location, c))
                .map(path -> formatter.include(path.toString()).trim())
                .collect(Collectors.joining("\n\n", "\n", "\n"));
    }

    private Path getRelativeFilePath(Path docPath, Class<?> clazz) {
        final Path classPath = DocumentationNamer.toPath(clazz, "", ".adoc");

        return docPath.relativize(classPath);
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
        final DocTestingDocumentation doc = new DocTestingDocumentation();
        new Document(doc.build()).saveAs(doc.getClass());
    }
}
