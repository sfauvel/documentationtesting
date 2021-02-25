package org.sfvl.doctesting;

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

public class ClassDocumentation {

    private final Formatter formatter;
    public Function<Method, Path> targetName;
    private static final PathProvider pathProvider = new PathProvider();
    protected Function<Method, DocumentationNamer> documentationNamerBuilder;

    public ClassDocumentation() {
        this(Paths.get("src", "test", "docs"));
    }

    public ClassDocumentation(Path docRootPath) {
        this(docRootPath, new AsciidocFormatter());
    }

    public ClassDocumentation(Path docRootPath, Formatter formatter) {
        this(m -> new DocumentationNamer(pathProvider.getProjectPath().resolve(docRootPath), m),
                formatter);
    }

    public ClassDocumentation(Function<Method, DocumentationNamer> documentationNamerBuilder, Formatter formatter) {
        this.formatter = formatter;
        this.documentationNamerBuilder = documentationNamerBuilder;
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
        return Optional.ofNullable(testClass.getAnnotation(DisplayName.class))
                .map(DisplayName::value)
                .orElse(formatClassNameToTitle(testClass));
    }

    private String formatClassNameToTitle(Class<?> testClass) {
        final String name = testClass.getSimpleName();
        return name.substring(0, 1) +
                name.substring(1)
                        .replaceAll("([A-Z])", " $1")
                        .toLowerCase();
    }

    protected String includeMethods(List<Method> testMethods, Path docFilePath) {
        Function<Method, Path> targetName = m -> documentationNamerBuilder.apply(m).getApprovedPath(docFilePath);
        return includeMethods(testMethods, targetName, 2);
    }

    protected String includeMethods(List<Method> testMethods, Function<Method, Path> targetPathName, final int levelOffset) {
        final Function<Path, String> includeWithOffset = path -> formatter.include(path.toString(), levelOffset).trim();
        // Trim because formatter add some line breaks (it may not add those line breaks)
        return MethodsOrder.sort(testMethods)
                .map(targetPathName)
                .map(includeWithOffset)
                .collect(Collectors.joining("\n"));
    }

    protected String joinParagraph(String... paragraph) {
        return Arrays.stream(paragraph)
                .filter(Objects::nonNull)
                .filter(t -> !t.trim().isEmpty())
                .collect(Collectors.joining("\n\n"));
    }

}
