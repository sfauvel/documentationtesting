package org.sfvl.doctesting;

import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.PathProvider;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generate a main documentation to group all test documentations.
 */
public class MainDocumentation {

    private static final String PACKAGE_TO_SCAN = "org.sfvl";
    private static final String DOCUMENTATION_FILENAME = "index";
    private static final PathProvider pathProvider = new PathProvider();
    private final String DOCUMENTATION_TITLE;
    private final Path docRootPath;

    public MainDocumentation() {
        this("Documentation");
    }

    public MainDocumentation(String documentationTitle) {
        DOCUMENTATION_TITLE = documentationTitle;
        docRootPath = Config.DOC_PATH.toAbsolutePath();
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
        final String testsDocumentation = getClassDocumentation(packageToScan);

        final String header = getHeader();

        return header + testsDocumentation;
    }

    protected String getClassDocumentation(String packageToScan) {
        final Stream<? extends Class<?>> testClasses = getAnnotatedMethod(Test.class, packageToScan).stream()
                .map(m -> m.getDeclaringClass())
                .distinct();

        String testDocumentation = testClasses
                .map(c -> new DocPath(c).approved().from(Config.DOC_PATH))
                .map(path -> "include::" + path + "[leveloffset=+2]")
                .collect(Collectors.joining("\n"));
        return testDocumentation;
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

    protected Set<Method> getAnnotatedMethod(Class<? extends Annotation> annotation, String packageToScan) {
        Reflections reflections = new Reflections(packageToScan, Scanners.MethodsAnnotated);
        return reflections.getMethodsAnnotatedWith(annotation);
    }

    public static void main(String... args) throws IOException {
        new MainDocumentation().generate(PACKAGE_TO_SCAN);
    }
}
