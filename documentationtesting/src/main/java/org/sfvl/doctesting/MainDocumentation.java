package org.sfvl.doctesting;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generate a main documentation to group all test documentations.
 */
public class MainDocumentation {

    private static final String PACKAGE_TO_SCAN = "org.sfvl";
    private final String DOCUMENTATION_TITLE;
    private static final String DOCUMENTATION_FILENAME = "Documentation";
    private final Path docRootPath;

    public MainDocumentation() {
        this("Documentation");
    }

    public MainDocumentation(String documentationTitle) {
        DOCUMENTATION_TITLE = documentationTitle;
        docRootPath = Paths.get(this.getClass().getClassLoader().getResource("").getPath())
                .resolve(Paths.get("..", "..", "src", "test", "docs"));

    }

    protected void generate(String packageToScan) throws IOException {

        Set<Method> testMethods = getAnnotatedMethod(Test.class, packageToScan);

        final Map<Class<?>, List<Method>> methodsByClass = testMethods.stream().collect(Collectors.groupingBy(
                m -> m.getDeclaringClass()
        ));

        String testsDocumentation = methodsByClass.entrySet().stream()
                .map(e -> "== "
                        + e.getKey().getSimpleName()
                        + "\n" + getComment(e.getKey())
                        + "\n\n"
                        + includeMethods(e.getValue())
                        + "\n\n"
                )
                .collect(Collectors.joining("\n"));

        System.out.println(testsDocumentation);

        final Path readmePath = Paths.get(this.getClass().getClassLoader().getResource("").getPath())
                .resolve(Paths.get("..", "..", "readme.adoc"));

        Path path = docRootPath.resolve(DOCUMENTATION_FILENAME + ".adoc");
        try (FileWriter fileWriter = new FileWriter(path.toFile())) {
            fileWriter.write(":toc: left\n:nofooter:\n\n");
            if (readmePath.toFile().exists()) {
                fileWriter.write("include::../../../readme.adoc[leveloffset=+1]\n\n");
            } else {
                fileWriter.write("= " + DOCUMENTATION_TITLE + "\n\n");
            }

            fileWriter.write(testsDocumentation);
        }

    }

    private String includeMethods(List<Method> testMethods) {
        return testMethods.stream()
                .map(m -> new DocumentationNamer(docRootPath, m))
                .map(m -> docRootPath.relativize(Paths.get(m.getSourceFilePath())) + "/" + m.getApprovalName() + ".approved.adoc")
                .map(m -> "include::" + m + "[leveloffset=+2]")
                .collect(Collectors.joining("\n"));
    }

    private String getComment(Class<?> clazz) {
        JavaProjectBuilder builder = new JavaProjectBuilder();

        final Path testPath = Paths.get(this.getClass().getClassLoader().getResource("").getPath())
                .resolve(Paths.get("..", "..", "src", "test", "java"));

//        builder.addSourceTree(new File("src/test/java"));
        builder.addSourceTree(testPath.toFile());

        JavaClass javaClass = builder.getClassByName(clazz.getCanonicalName());

        return Optional.ofNullable(javaClass.getComment()).orElse("");
    }

    private Set<Method> getAnnotatedMethod(Class<? extends Annotation> annotation, String packageToScan) {
        Reflections reflections = new Reflections(packageToScan, new MethodAnnotationsScanner());
        return reflections.getMethodsAnnotatedWith(annotation);
    }


    public static void main(String... args) throws IOException {
        new MainDocumentation().generate(PACKAGE_TO_SCAN);
    }
}
