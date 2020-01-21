package org.sfvl.doctesting;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaPackage;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generate a main documentation to group all test documentations.
 */
public class MainDocumentation {

    private static final String PACKAGE_TO_SCAN = "org.sfvl";
    private static final String DOCUMENTATION_TITLE = "Documentation";
    private static final String DOCUMENTATION_FILENAME = "Documentation";

    private void generate(String packageToScan) throws IOException {
        Set<Method> testMethods = getAnnotatedMethod(Test.class, packageToScan);

        final Map<String, List<Method>> methodsByClass = testMethods.stream().collect(Collectors.groupingBy(
                m -> m.getDeclaringClass().getSimpleName()
        ));

        String testsDocumentation = methodsByClass.entrySet().stream()
            .map(e -> "== " + e.getKey() + "\n\n:leveloffset: +1\n" + includeMethods(e.getValue()) + "\n:leveloffset: -1\n")
                .collect(Collectors.joining("\n"));

        System.out.println(testsDocumentation);

        Path path = DocumentationNamer.DOC_ROOT_PATH.resolve(DOCUMENTATION_FILENAME + ".adoc");
        try (FileWriter fileWriter = new FileWriter(path.toFile())) {
            fileWriter.write("= " + DOCUMENTATION_TITLE + "\n:toc: left\n\n");
            fileWriter.write(testsDocumentation);
        }

    }

    private String includeMethods(List<Method> testMethods) {
        return testMethods.stream()
                    .map(DocumentationNamer::new)
                    .map(m -> DocumentationNamer.DOC_ROOT_PATH.relativize(Paths.get(m.getSourceFilePath())) + "/" + m.getApprovalName() + ".approved.adoc")
                    .map(m -> "include::" + m + "[leveloffset=+1]")
                    .collect(Collectors.joining("\n"));
    }

    private Set<Method> getAnnotatedMethod(Class<? extends Annotation> annotation, String packageToScan) {
        Reflections reflections = new Reflections(packageToScan, new MethodAnnotationsScanner());
        return reflections.getMethodsAnnotatedWith(annotation);
    }


    public static void main(String... args) throws IOException {
        new MainDocumentation().generate(PACKAGE_TO_SCAN);
    }
}
