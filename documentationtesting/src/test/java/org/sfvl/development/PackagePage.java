package org.sfvl.development;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.sfvl.codeextraction.ClassFinder;
import org.sfvl.codeextraction.CodePath;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitinheritance.ApprovalsBase;
import org.sfvl.doctesting.writer.ClassDocumentation;
import org.sfvl.doctesting.writer.Classes;
import org.sfvl.test_tools.IntermediateHtmlPage;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;


@ExtendWith(IntermediateHtmlPage.class)
public class PackagePage {

    public String build_classes_to_include(Classes classesBuilder) {
        final ClassFinder classFinder = new ClassFinder();
        final List<Class<?>> classes = classFinder.classesWithAnnotatedMethod(this.getClass().getPackage(), Test.class, this::toBeInclude);
        classes.remove(this.getClass());

        final Path location = CodePath.toPath(this.getClass().getPackage());
        return classesBuilder.includeClasses(location, classes, 0);
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

}
