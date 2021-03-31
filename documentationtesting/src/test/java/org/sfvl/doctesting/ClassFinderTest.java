package org.sfvl.doctesting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;

import java.util.List;
import java.util.stream.Collectors;

public class ClassFinderTest {
    private static final DocWriter doc = new DocWriter();
    @RegisterExtension
    static ApprovalsExtension extension = new ApprovalsExtension(doc);

    @Test
    public void find_test_classes_in_a_package(TestInfo testInfo) {
        // >>>
        List<Class<?>> classes = new ClassFinder()
                .testClasses(org.sfvl.doctesting.sample.SimpleClass.class.getPackage());
        // <<<

        final String classesFound = classes.stream()
                .map(Class::getCanonicalName)
                .map(s -> String.format("* %s",s))
                .collect(Collectors.joining("\n", "", "\n"));

        final AsciidocFormatter formatter = new AsciidocFormatter();
        doc.write(".Source code to find classes containing tests",
                formatter.sourceCode(CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get())),
                "Classes found:",
                "" ,
                classesFound);
    }

    /**
     * By default, all classes containing a test method are found.
     * It's possible to exclude some test methods because they are not relevant in the context.
     * If all test methods of a class are excluded, then the class will not be return as a found class.
     */
    @Test
    public void find_test_classes_in_a_package_with_filter(TestInfo testInfo) {
        // >>>
        List<Class<?>> classes = new ClassFinder()
                .testClasses(org.sfvl.doctesting.sample.SimpleClass.class.getPackage(),
                    m -> !m.getDeclaringClass().getSimpleName().startsWith("Second"));
        // <<<

        final String classesFound = classes.stream()
                .map(Class::getCanonicalName)
                .map(s -> String.format("* %s",s))
                .collect(Collectors.joining("\n", "", "\n"));

        final AsciidocFormatter formatter = new AsciidocFormatter();
        doc.write("Here, we exclude all method in a class with name starting by 'Second'.",
                ".Source code to find classes containing tests",
                formatter.sourceCode(CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get())),
                "Classes found:",
                "" ,
                classesFound);
    }


}
