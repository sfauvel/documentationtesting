package org.sfvl.doctesting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.Formatter;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.utils.*;
import org.sfvl.doctesting.writer.Classes;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;

public class DocTestingDocumentation {

    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    protected final Formatter formatter = new AsciidocFormatter();
    private static final ClassFinder classFinder = new ClassFinder();

    @Test
    @NoTitle
    public void doc() {
        doc.write(build());
    }

    public String build() {
        return formatter.paragraphSuite(
                generalInformation(),
                includeClasses()
                );
    }

    protected String generalInformation() {
        return formatter.paragraphSuite(
                "This document describes usage of classes to create test from generated documentation.",
                "* <<" + ApprovalsExtension.class.getSimpleName() + ">>: JUnit extension to check document.",
                "* <<" + DocWriter.class.getSimpleName() + ">>: Store document before writting it.",
                "* <<" + CodeExtractor.class.getSimpleName() + ">>: Help to extract information from code.");
    }

    public String includeClasses() {
        final Path location = DocPath.toPath(DocTestingDocumentation.class.getPackage());
        return new Classes(formatter).includeClasses(location, getClassesToDocument(), 0);
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
        final List<Class<?>> classes = classFinder.testClasses(DocTestingDocumentation.class.getPackage(),
                this::toBeInclude);
        classes.remove(this.getClass());
        return classes;
    }


}
