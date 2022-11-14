package org.sfvl.codeextraction;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.DocTestingDocumentation;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitinheritance.ApprovalsBase;
import org.sfvl.doctesting.utils.NoTitle;
import org.sfvl.doctesting.writer.ClassDocumentation;
import org.sfvl.doctesting.writer.Classes;
import org.sfvl.test_tools.FastApprovalsExtension;
import org.sfvl.test_tools.IntermediateHtmlPage;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;

// TODO this class is very close to DocTestingDocumentation.java. Need to extract duplicated code.
@ExtendWith(IntermediateHtmlPage.class)
@DisplayName("Tools to extract code")
public class CodeExtractionPackage {

    @RegisterExtension
    static ApprovalsExtension doc = new FastApprovalsExtension();

    private static final ClassFinder classFinder = new ClassFinder();

    @Test
    @NoTitle
    public void doc() {
        doc.write(build());
    }

    public String build() {
        return doc.getFormatter().paragraphSuite(
//                generalInformation(),
                includeClasses()
        );
    }


    public String includeClasses() {
        final Path location = CodePath.toPath(DocTestingDocumentation.class.getPackage());
        return new Classes(doc.getFormatter()).includeClasses(location, getClassesToDocument(), 0);
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
        final List<Class<?>> classes = classFinder.classesWithAnnotatedMethod(this.getClass().getPackage(), Test.class, this::toBeInclude);
        classes.remove(this.getClass());
        return classes;
    }
}

