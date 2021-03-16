package org.sfvl.doctesting;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.sfvl.docformatter.Formatter;

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
public class MainDocumentation extends ClassDocumentation {

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

    public MainDocumentation(String documentationTitle,
                             Path docRootPath,
                             Function<Method, Path> methodToPath,
                             Formatter formatter) {
        super(formatter, methodToPath, m -> m.isAnnotationPresent(Test.class), c -> c.isAnnotationPresent(Nested.class));
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
        generate(packageToScan, DocumentationNamer.toPath(getClass()).toString());
    }

    protected void generate(String packageToScan) throws IOException {
        generate(packageToScan, DOCUMENTATION_FILENAME);
    }

    public void generate(String packageToScan, String documentationFilename) throws IOException {
        final Path docFilePath = docRootPath.resolve(documentationFilename + ".adoc");
        final String content = getDocumentationContent(packageToScan, docFilePath.getParent());

        try (FileWriter fileWriter = new FileWriter(docFilePath.toFile())) {
            writeDoc(fileWriter, content);
        }
    }

    protected void generate(Class<?> classToGenerate) throws IOException {
        final Path docFilePath = docRootPath.resolve(DocumentationNamer.toPath(this.getClass(), "", ".adoc"));

        final DocWriter doc = new DocWriter();
        doc.write(":source-highlighter: rouge",
                getDocumentOptions(),
                "",
                new ClassDocumentation().getClassDocumentation(classToGenerate));

        final String content = doc.read();

        try (FileWriter fileWriter = new FileWriter(docFilePath.toFile())) {
            fileWriter.write(content);
        }
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
        final int title_depth = 0;
        return getMethodDocumentation(packageToScan, docFilePath, title_depth);
    }

    protected String getMethodDocumentation(String packageToScan, Path docFilePath, int title_depth) {
        Set<Method> testMethods = getAnnotatedMethod(Test.class, packageToScan);

        final Map<Class<?>, List<Method>> methodsByClass = testMethods.stream()
                .collect(Collectors.groupingBy(method -> CodeExtractor.getFirstEnclosingClassBefore(method, null)));

        final Set<Class<?>> classes = methodsByClass.keySet();
        return classes.stream()
                .sorted(Comparator.comparing(Class::getSimpleName))
                .map(c -> {
                    final Path mainDocPath = docFilePath;
                    final Path classPath = DocumentationNamer.toPath(c, "", ".adoc");
                    final Path finalPath = mainDocPath.relativize(getDocRootPath().resolve(classPath));

                    return finalPath;
                })
                .map(s -> formatter.include(s.toString(), title_depth + 1))
                .collect(Collectors.joining("\n\n"));
    }

    protected String getHeader() {
        final Path readmePath = pathProvider.getProjectPath().resolve(Paths.get("readme.adoc"));

        final String header = formatter.paragraphSuite(
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

    protected String getComment(Class<?> clazz) {
        return CodeExtractor.getComment(clazz);
    }

    protected Set<Method> getAnnotatedMethod(Class<? extends Annotation> annotation, String packageToScan) {
        Reflections reflections = new Reflections(packageToScan, new MethodAnnotationsScanner());
        return reflections.getMethodsAnnotatedWith(annotation);
    }

}
