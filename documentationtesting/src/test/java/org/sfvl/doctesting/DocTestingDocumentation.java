package org.sfvl.doctesting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.codeextraction.ClassFinder;
import org.sfvl.codeextraction.CodeExtractor;
import org.sfvl.codeextraction.CodePath;
import org.sfvl.docformatter.Formatter;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.ApprovalsExtensionTest;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.junitinheritance.ApprovalsBase;
import org.sfvl.doctesting.utils.*;
import org.sfvl.doctesting.writer.ClassDocumentation;
import org.sfvl.doctesting.writer.Classes;
import org.sfvl.doctesting.writer.DocWriter;
import org.sfvl.test_tools.IntermediateHtmlPage;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;

@ExtendWith(IntermediateHtmlPage.class)
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
                "* " + makeAnchor(ApprovalsExtensionTest.class, ApprovalsExtension.class) + ": JUnit extension to check document.",
                "* " + makeAnchor(DocWriterTest.class, DocWriter.class) + ": Store document before writting it.",
                "* " + makeAnchor(CodeExtractorTest.class, CodeExtractor.class) + ": Help to extract information from code.");
    }

    private String makeAnchor(Class<?> clazzAnchor, Class<?> clazzNameToDisplay) {
        return String.format("<<%s,%s>>", doc.getDocWriter().titleId(clazzAnchor), clazzNameToDisplay.getSimpleName());
    }

    public String includeClasses() {
        final Path location = CodePath.toPath(DocTestingDocumentation.class.getPackage());
        return new Classes(formatter).includeClasses(location, getClassesToDocument(), 0);
    }

    public boolean toBeInclude(Class<?> clazz) {
        if (clazz == null) {
            return true;
        }
        if (clazz.getPackage().equals(ApprovalsBase.class.getPackage())
                || clazz.getPackage().equals(ClassDocumentation.class.getPackage())) {
            return false;
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
