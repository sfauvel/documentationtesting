package org.sfvl.doctesting.writer;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.utils.*;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * You can use this class to generate a main documentation that aggregate other documentations
 * and in particular, those generated from test classes.
 *
 * @deprecated
 * This class was used to create a global documentation very quickly
 * but it hides to many things without simplifying much
 * so we prefer to not use it.
 */
@Deprecated
public class MainDocumentation {

    protected final String documentationTitle;
    private static final String DOCUMENTATION_FILENAME = "Documentation";
    private static final PathProvider pathProvider = new PathProvider();
    protected final Formatter formatter;
    private final Path docRootPath;
    private static final ClassFinder classFinder = new ClassFinder();

    public MainDocumentation withPackageLocation(Package packageLocation) {
        this.packageLocation = packageLocation;
        return this;
    }

    public MainDocumentation withClassesToInclude(Class<?>... classesToInclude) {
        return withClassesToInclude(Arrays.asList(classesToInclude));
    }

    public MainDocumentation withClassesToInclude(List<Class<?>> classesToInclude) {
        this.classesToInclude = classesToInclude;
        return this;
    }

    private Package packageLocation;
    private List<Class<?>> classesToInclude;

    public MainDocumentation() {
        this("Documentation");
    }

    public MainDocumentation(String documentationTitle) {
        this.documentationTitle = documentationTitle;
        this.docRootPath = Config.DOC_PATH;
        this.formatter = new AsciidocFormatter();
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
        generate(packageToScan, new DocPath(getClass()).page());
    }

    protected void generate(String packageToScan) throws IOException {
        final OnePath onePath = new OnePath(Config.DOC_PATH, Paths.get(""), DOCUMENTATION_FILENAME, ".adoc");
        generate(packageToScan, onePath);
    }

    private void generate(String packageToScan, OnePath onePath) throws IOException {
        final String content = getDocumentationContent(packageToScan, onePath.folder());

        try (FileWriter fileWriter = new FileWriter(onePath.path().toFile())) {
            writeDoc(fileWriter, content);
        }
    }

    protected void generate(Class<?> classToGenerate) throws IOException {
        final Path docFilePath = new DocPath(this.getClass()).page().path();

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
        return getMethodDocumentation(getClassesWithTest(packageToScan), docFilePath, title_depth);
    }

    protected String getMethodDocumentation(Set<Class<?>> classes, Path docFilePath, int title_depth) {

        return classes.stream()
                .sorted(Comparator.comparing(Class::getSimpleName))
                .map(c -> new DocPath(c).page().from(docFilePath))
                .map(s -> formatter.include(s.toString(), title_depth + 1))
                .collect(Collectors.joining("\n\n"));
    }

    private Set<Class<?>> getClassesWithTest(String packageToScan) {

        return getAnnotatedMethod(Test.class, packageToScan).stream()
                .map(method -> classFinder.getMainFileClass(method.getDeclaringClass()))
                .collect(Collectors.toSet());
    }

    protected String getHeader() {
        final Path readmePath = pathProvider.getProjectPath().resolve(Paths.get("readme.adoc"));

        final String header = formatter.paragraphSuite(
                getDocumentOptions(),
                (readmePath.toFile().exists()
                        ? "include::../../../readme.adoc[leveloffset=+1]"
                        : "= " + documentationTitle),
                generalInformation());
        return header;
    }

    protected String generalInformation() {
        return "";
    }



    public static class Option {
        String key;
        String value;

        public Option(String key) {
            this(key, "");
        }

        public Option(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String format() {
            return String.format(":%s: %s", key, value).trim();
        }

    }

    private final List<Option> options = new ArrayList<Option>() {{
        add(new Option("toc", "left"));
        add(new Option("nofooter"));
        add(new Option("stem"));
    }};

    protected String getDocumentOptions() {
        return options.stream()
                .map(Option::format)
                .collect(Collectors.joining("\n"));
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
