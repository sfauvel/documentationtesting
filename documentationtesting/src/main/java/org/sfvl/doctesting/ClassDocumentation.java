package org.sfvl.doctesting;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaModel;
import org.junit.jupiter.api.DisplayName;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.junitextension.ClassToDocument;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassDocumentation {

    private final Formatter formatter;
    public Function<Method, Path> targetName;
    public final Path docRootPath;
    private static final PathProvider pathProvider = new PathProvider();

    public ClassDocumentation() {
        this(Paths.get("src", "test", "docs"));
    }

    public ClassDocumentation(Path docRootPath) {
        this(docRootPath, new AsciidocFormatter());
    }

    public ClassDocumentation(Path docRootPath, Formatter formatter) {
        this.docRootPath = pathProvider.getProjectPath().resolve(docRootPath);
        this.formatter = formatter;
    }

    public String getClassDocumentation(Class<?> clazz, List<Method> methods, Function<Method, Path> targetName, int depth) {

        final Map<Class<?>, List<Method>> methodsByClass = methods.stream()
                .collect(Collectors.groupingBy(method -> CodeExtractor.getFirstEnclosingClassBefore(method, clazz)));

        String content = (methodsByClass.size() == 1)
                ? includeMethods(methods, targetName, depth)
                : formatClasses(targetName, depth, methodsByClass);

        return joinParagraph(
                getTitle(clazz, depth),
                getDescription(clazz),
                content);
    }

    private String formatClasses(Function<Method, Path> targetName, int depth, Map<Class<?>, List<Method>> methodsByClass) {
        return methodsByClass.entrySet().stream()
                // TODO We have to sort by position in file like methods and not by name.
                .sorted(Comparator.comparing(e -> e.getKey().getSimpleName()))
                .map(e -> getClassDocumentation(e.getKey(), e.getValue(), targetName, depth + 1))
                .collect(Collectors.joining("\n\n"));
    }

    public String getTitle(Class<?> clazz, int depth) {
        return new String(new char[depth]).replace("\0", "=") + " " + getTestClassTitle(clazz);
    }

    protected String getDescription(Class<?> classToDocument) {
        return joinParagraph(
                relatedClassDescription(classToDocument).orElse(""),
                CodeExtractor.getComment(classToDocument));
    }

    private Optional<String> relatedClassDescription(Class<?> fromClass) {
        return Optional.ofNullable(fromClass.getAnnotation(ClassToDocument.class))
                .map(ClassToDocument::clazz)
                .map(CodeExtractor::getComment);
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
        Function<Method, Path> targetName = m -> getRelativizedPath(new DocumentationNamer(this.docRootPath, m), docFilePath);
        return includeMethods(testMethods, targetName, 2);
    }

    protected String includeMethods(List<Method> testMethods, Function<Method, Path> targetPathName, final int levelOffset) {
        final Function<Path, String> includeWithOffset = path -> formatter.include(path.toString(), levelOffset).trim();
        // Trim because formatter add some line breaks (it may not add those line breaks)
        return getMethodsInOrder(testMethods)
                .map(targetPathName)
                .map(includeWithOffset)
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

}
