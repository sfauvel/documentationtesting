package org.sfvl.doctesting;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generate a main documentation to group all test documentations.
 */
public class MainDocumentation {

    private static final String PACKAGE_TO_SCAN = "org.sfvl";
    protected final String DOCUMENTATION_TITLE;
    private static final String DOCUMENTATION_FILENAME = "Documentation";
    private static final PathProvider pathProvider = new PathProvider();
    private final Path docRootPath;
    private Path docFilePath;

    public MainDocumentation() {
        this("Documentation");
    }

    public MainDocumentation(String documentationTitle) {
        this(documentationTitle, Paths.get("src", "test", "docs"));
    }

    public MainDocumentation(String documentationTitle, Path docRootPath) {
        this.DOCUMENTATION_TITLE = documentationTitle;
        this.docRootPath = pathProvider.getProjectPath().resolve(docRootPath);
    }

    public Path getDocRootPath() {
        return docRootPath;
    }


    public void generate() throws IOException {
        final String packageToScan = getClass().getPackage().getName();
        final String documentationFilename = getClass().getSimpleName();
        final String replace = packageToScan.replace(".", "/");
        generate(packageToScan, Paths.get(replace).resolve(documentationFilename).toString());
    }

    protected void generate(String packageToScan) throws IOException {
        generate(packageToScan, DOCUMENTATION_FILENAME);
    }

    public void generate(String packageToScan, String documentationFilename) throws IOException {
        docFilePath = docRootPath.resolve(documentationFilename + ".adoc");
        final String content = getDocumentationContent(packageToScan);

        try (FileWriter fileWriter = new FileWriter(docFilePath.toFile())) {
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
                .sorted(Comparator.comparing(e -> e.getKey().getSimpleName()))
                .map(e -> "== "
                        + getTestClassTitle(e)
                        + "\n" + CodeExtractor.getComment(e.getKey())
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

        final String header = getDocumentOptions() +
                (readmePath.toFile().exists()
                        ? "include::../../../readme.adoc[leveloffset=+1]\n\n"
                        : "= " + DOCUMENTATION_TITLE + "\n\n") +
                explanation() +
                "View source project on link:{github-repo}/" + projectFolderPath.toString() + "[Github]\n\n";
        ;
        return header;
    }

    private String explanation() {

        final Path relativizeToRoot = pathProvider.getProjectPath().relativize(pathProvider.getGitRootPath());

        return "NOTE: The examples shown here are generated from the source code.\n" +
                "They therefore represent the behavior of the application at any times.\n" +
                "Non regression, as for it, is ensured by checking the absence of change in this document.\n" +
                "Learn more here link:{github-pages}[]\n\n";
    }

    protected String getDocumentOptions() {
        return ":toc: left\n:nofooter:\n:stem:\n\n";
    }

    private void writeDoc(FileWriter fileWriter, String content) throws IOException {
        fileWriter.write(content);
    }

    protected String getTestClassTitle(Map.Entry<Class<?>, List<Method>> e) {
        Class<?> testClass = e.getKey();
        DisplayName annotation = testClass.getAnnotation(DisplayName.class);
        if (annotation != null) {
            return annotation.value();
        } else {
            return testClass.getSimpleName();
        }
    }

    protected String includeMethods(List<Method> testMethods) {

        return getMethodsInOrder(testMethods)
                .map(m -> new DocumentationNamer(docRootPath, m))
                .map(m -> {
                    final String filename = m.getApprovalName() + ".approved.adoc";
                    return docFilePath.getParent().relativize(Paths.get(m.getSourceFilePath())).resolve(filename);
                })
                .map(m -> "include::" + m + "[leveloffset=+2]")
                .collect(Collectors.joining("\n"));
    }

    private Stream<Method> getMethodsInOrder(List<Method> testMethods) {
        Map<String, Method> methodsByName = testMethods.stream().collect(Collectors.toMap(
                Method::getName,
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

    protected String getComment(Class<?> clazz) {
        return CodeExtractor.getComment(clazz);
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
