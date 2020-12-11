package org.sfvl.doctesting;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.sfvl.doctesting.junitextension.ClassToDocument;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
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

    public Path getGitRootPath() {
        return pathProvider.getGitRootPath();
    }

    public Path getProjectPath() {
        return pathProvider.getProjectPath();
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
        final Path docFilePath = docRootPath.resolve(documentationFilename + ".adoc");
        final String content = getDocumentationContent(packageToScan, docFilePath);

        try (FileWriter fileWriter = new FileWriter(docFilePath.toFile())) {
            writeDoc(fileWriter, content);
        }
    }

    private <K, V> String mapToString(Map<K, V> map, Function<Map.Entry<K, V>, List<String>> transform, String delimiter) {

        return map.entrySet().stream()
                .map(transform)
                .flatMap(Collection::stream)
                .collect(Collectors.joining(delimiter)
                );
    }

    private <K, V> String mapToString(Map<K, V> map, BiFunction<K, V, String> transform, String delimiter, Comparator<Map.Entry<K, V>> comparator) {
        return streamToString(
                map.entrySet().stream().sorted(comparator),
                transform,
                delimiter);
    }

    private <K, V> String mapToString(Map<K, V> map, BiFunction<K, V, String> transform, String delimiter) {
        return streamToString(
                map.entrySet().stream(),
                transform,
                delimiter);
    }

    private <K, V> String streamToString(Stream<Map.Entry<K, V>> stream, BiFunction<K, V, String> transform, String delimiter) {
        return stream.map(e -> transform.apply(e.getKey(), e.getValue()))
                .collect(Collectors.joining(delimiter)
                );
    }

    /**
     * Documentation is composed with the header following by each test classes documentation.
     *
     * A class documentation is composed with a title and a description.
     * Each test method has created a file which is included  to document the class.
     *
     * You can overwrite methods to modify document generated.
     *
     * @param packageToScan
     * @param docFilePath
     * @return
     */
    protected String getDocumentationContent(String packageToScan, Path docFilePath) {
        return String.join("\n\n",
                getHeader(),
                getMethodDocumentation(packageToScan, docFilePath)
        );
    }

    // TODO rename this  method. It extract content but there is already a getDocumentationContent
    protected String getMethodDocumentation(String packageToScan, Path docFilePath) {
        Set<Method> testMethods = getAnnotatedMethod(Test.class, packageToScan);

        final Map<Class<?>, List<Method>> methodsByClass = testMethods.stream()
                .collect(Collectors.groupingBy(method -> CodeExtractor.getFirstEnclosingClassBefore(method, null)));

        BiFunction<Class<?>, List<Method>, String> documentClass = (clazz, methods) -> getClassDocumentation(clazz, methods, docFilePath, 2);

        return mapToString(methodsByClass, documentClass, "\n\n", Comparator.comparing(e -> e.getKey().getSimpleName()));
    }

    private String getClassDocumentation(Class<?> clazz, List<Method> methods, Path docFilePath, int depth) {
        final Map<Class<?>, List<Method>> methodsByClass = methods.stream()
                .collect(Collectors.groupingBy(method -> CodeExtractor.getFirstEnclosingClassBefore(method, clazz)));

        String content = (methodsByClass.size() == 1)
                ? includeMethods(methods, docFilePath, depth)
                :  methodsByClass.entrySet().stream()
                // TODO We have to sort by position in file like methods and not by name.
                .sorted(Comparator.comparing(e -> e.getKey().getSimpleName()))
                .map(e -> getClassDocumentation(e.getKey(), e.getValue(), docFilePath, depth + 1))
                .collect(Collectors.joining("\n\n"));

        return joinParagraph(
                        new String(new char[depth]).replace("\0", "=") + " " + getTestClassTitle(clazz),
                        getDescription(clazz),
                        content);
    }

    protected String getDescription(Class<?> classToDocument) {
        List<String> description = new ArrayList<>();

        final ClassToDocument annotation = classToDocument.getAnnotation(ClassToDocument.class);
        if (annotation != null) {
            final Class<?> clazz = annotation.clazz();
            final String comment = CodeExtractor.getComment(clazz);
            description.add(comment);
        }
        description.add(CodeExtractor.getComment(classToDocument));
        return description.stream()
                .collect(Collectors.joining("\n\n"));
    }

    protected String getHeader() {
        final Path readmePath = pathProvider.getProjectPath().resolve(Paths.get("readme.adoc"));

        final String header = joinParagraph(
                getDocumentOptions(),
                (readmePath.toFile().exists()
                        ? "include::../../../readme.adoc[leveloffset=+1]"
                        : "= " + DOCUMENTATION_TITLE),
                generalInformation());
        return header;
    }

    protected String generalInformation() {
        return "";
    }

    protected String getDocumentOptions() {
        return ":toc: left\n:nofooter:\n:stem:";
    }

    private void writeDoc(FileWriter fileWriter, String content) throws IOException {
        fileWriter.write(content);
    }

    protected String getTestClassTitle(Map.Entry<Class<?>, List<Method>> e) {
        return getTestClassTitle(e.getKey());
    }

    protected String getTestClassTitle(Class<?> testClass) {
        DisplayName annotation = testClass.getAnnotation(DisplayName.class);
        if (annotation != null) {
            return annotation.value();
        } else {
            final String name = testClass.getSimpleName();
            return name.substring(0,1) +
                    name.substring(1)
                            .replaceAll("([A-Z])", " $1")
                            .toLowerCase();
        }
    }

    protected String includeMethods(List<Method> testMethods, Path docFilePath) {
        return includeMethods(testMethods, docFilePath, 2);
    }

    protected String includeMethods(List<Method> testMethods, Path docFilePath, final int leveloffset) {

        return getMethodsInOrder(testMethods)
                .map(m -> new DocumentationNamer(docRootPath, m))
                .map(m -> getRelativizedPath(m, docFilePath))
                .map(m -> String.format("include::%s[leveloffset=+%d]", m, leveloffset))
                .collect(Collectors.joining("\n"));
    }

    protected Path getRelativizedPath(DocumentationNamer m, Path docFilePath) {
        final String filename = m.getApprovalName() + ".approved.adoc";
        return docFilePath.getParent().relativize(Paths.get(m.getSourceFilePath())).resolve(filename);
    }

    private Stream<Method> getMethodsInOrder(List<Method> testMethods) {
        Map<String, Method> methodsByName = testMethods.stream().collect(Collectors.toMap(
                Method::getName,
                m -> m
        ));

        JavaProjectBuilder builder = createJavaProjectBuilderWithTestPath();

        Method firstMethod = testMethods.get(0);
        JavaClass javaClass = builder.getClassByName(firstMethod.getDeclaringClass().getName());

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

    protected String joinParagraph(String... paragraph) {
        return Arrays.stream(paragraph)
                .filter(Objects::nonNull)
                .filter(t -> !t.trim().isEmpty())
                .collect(Collectors.joining("\n\n"));
    }

    public static void main(String... args) throws IOException {
        new MainDocumentation().generate(PACKAGE_TO_SCAN);
    }

}
