package org.sfvl.doctesting;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaModel;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generate a main documentation to group all test documentations.
 */
public class MainDocumentation {

    private static final String PACKAGE_TO_SCAN = "org.sfvl";
    private final String DOCUMENTATION_TITLE;
    private static final String DOCUMENTATION_FILENAME = "index";
    private static final PathProvider pathProvider = new PathProvider();
    private final Path docRootPath;

    public MainDocumentation() {
        this("Documentation");
    }

    public MainDocumentation(String documentationTitle) {
        DOCUMENTATION_TITLE = documentationTitle;
        docRootPath = pathProvider.getProjectPath().resolve(Paths.get("src", "test", "docs"));
    }

    public Path getDocRootPath() {
        return docRootPath;
    }

    protected void generate(String packageToScan) throws IOException {
        generate(packageToScan, DOCUMENTATION_FILENAME);
    }

    protected void generate(String packageToScan, String documentationFilename) throws IOException {
        final String content = getDocumentationContent(packageToScan);

        Path path = docRootPath.resolve(documentationFilename + ".adoc");
        try (FileWriter fileWriter = new FileWriter(path.toFile())) {
            writeDoc(fileWriter, content);
        }
    }

    protected String getDocumentationContent(String packageToScan) {
        final String testsDocumentation = getMethodDocumentation(packageToScan);

        final String header = getHeader();

        return header + testsDocumentation;
    }

    protected String getMethodDocumentation(String packageToScan) {
        Set<Method> testMethods = getAnnotatedMethod(Test.class, packageToScan);

        final Map<Class<?>, List<Method>> methodsByClass = testMethods.stream().collect(Collectors.groupingBy(
                m -> m.getDeclaringClass()
        ));

        String testsDocumentation = methodsByClass.entrySet().stream()
                .map(e -> "== "
                        + getTestClassTitle(e)
                        + "\n" + getComment(e.getKey())
                        + "\n\n"
                        + includeMethods(e.getValue())
                        + "\n\n"
                )
                .collect(Collectors.joining("\n"));

        //System.out.println(testsDocumentation);
        return testsDocumentation;
    }

    protected String getHeader() {
        final Path readmePath = pathProvider.getProjectPath().resolve(Paths.get("readme.adoc"));

        final Path projectFolderPath = pathProvider.getGitRootPath().relativize(pathProvider.getProjectPath());

        final String header = ":toc: left\n:nofooter:\n:stem:\n\n" +
                (readmePath.toFile().exists()
                        ? "include::../../../readme.adoc[leveloffset=+1]\n\n"
                        : "= " + DOCUMENTATION_TITLE + "\n\n") +
                "View source project on link:{github-repo}/" + projectFolderPath.toString() + "[Github]\n\n";
        ;
        return header;
    }

    private void writeDoc(FileWriter fileWriter, String content) throws IOException {
        fileWriter.write(content);
    }

    protected String getTestClassTitle(Map.Entry<Class<?>, List<Method>> e) {
        Class<?> testClass = e.getKey();
        return formatTitle(testClass.getSimpleName());
    }

    protected String includeMethods(List<Method> testMethods) {

        return getMethodsInOrder(testMethods)
                .map(m -> new DocumentationNamer(docRootPath, m))
                .map(m -> docRootPath.relativize(Paths.get(m.getSourceFilePath())) + "/" + m.getApprovalName() + ".approved.adoc")
                .map(m -> "include::" + m + "[leveloffset=+2]")
                .collect(Collectors.joining("\n"));
    }

    private Stream<Method> getMethodsInOrder(List<Method> testMethods) {
        Map<String, Method> methodsByName = testMethods.stream().collect(Collectors.toMap(
                m -> m.getName(),
                m -> m
        ));

        JavaProjectBuilder builder = createJavaProjectBuilderWithTestPath();

        Method firstMethod = testMethods.get(0);
        JavaClass javaClass = builder.getClassByName(firstMethod.getDeclaringClass().getCanonicalName());

        return javaClass.getMethods().stream()
                .filter(m -> methodsByName.containsKey(m.getName()))
                .sorted(Comparator.comparingInt(JavaModel::getLineNumber))
                .map(m -> methodsByName.get(m.getName()));

    }

    private String formatTitle(String methodName) {
        String title = methodName
                .replace("_", " ");

        return title.substring(0, 1).toUpperCase() + title.substring(1);
    }

    protected String getComment(Class<?> clazz) {
        JavaProjectBuilder builder = createJavaProjectBuilderWithTestPath();

        JavaClass javaClass = builder.getClassByName(clazz.getCanonicalName());

        return Optional.ofNullable(javaClass.getComment()).orElse("");
    }

    protected Set<Method> getAnnotatedMethod(Class<? extends Annotation> annotation, String packageToScan) {
        Reflections reflections = new Reflections(packageToScan, new MethodAnnotationsScanner());
        return reflections.getMethodsAnnotatedWith(annotation);
    }

    private JavaProjectBuilder createJavaProjectBuilderWithTestPath() {
        JavaProjectBuilder builder = new JavaProjectBuilder();

        final Path testPath = pathProvider.getProjectPath().resolve(Paths.get("src", "test", "java"));
        builder.addSourceTree(testPath.toFile());
        return builder;
    }

    public static void main(String... args) throws IOException {
        new MainDocumentation().generate(PACKAGE_TO_SCAN);
    }
}
