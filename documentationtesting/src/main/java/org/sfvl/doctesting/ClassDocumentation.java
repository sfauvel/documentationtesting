package org.sfvl.doctesting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.junitextension.ClassToDocument;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generate a documentation of a test class aggregating approved method files.
 */
public class ClassDocumentation {

    private static final PathProvider pathProvider = new PathProvider();

    protected final Formatter formatter;
    protected final BiFunction<Method, Path, Path> methodToPath;
    private final Predicate<Method> methodFilter;
    private final Predicate<Class> classFilter;

    public ClassDocumentation() {
        this(new AsciidocFormatter(), (m, p) -> new DocumentationNamer(pathProvider.getProjectPath().resolve(Paths.get("src", "test", "docs")), m).getApprovedPath(p)
        );
    }

    public ClassDocumentation(Formatter formatter, BiFunction<Method, Path, Path> methodToPath) {
        this(formatter, methodToPath, m -> m.isAnnotationPresent(Test.class), c -> c.isAnnotationPresent(Nested.class));
    }

    public ClassDocumentation(Formatter formatter, BiFunction<Method, Path, Path> methodToPath, Predicate<Method> methodFilter, Predicate<Class> classFilter) {
        this.formatter = formatter;
        this.methodToPath = methodToPath;
        this.methodFilter = methodFilter;
        this.classFilter = classFilter;
    }

    public String getClassDocumentation(Class<?> clazz) {
        return getClassDocumentation(clazz, 1);
    }

    public String getClassDocumentation(Class<?> clazz, int depth) {

        return getMyClassDocumentation(clazz, depth);
    }

    private String getMyClassDocumentation(Class<?> clazz, int depth) {
        final ClassesOrder classesOrder = new ClassesOrder();

        final Stream<ClassesOrder.EncapsulateDeclared> declaredInOrder = classesOrder.getDeclaredInOrder(
                clazz,
                this.methodFilter,
                classFilter);

        return getMyClassDocumentation(clazz, depth, declaredInOrder.collect(Collectors.toList()));
    }
    private String getMyClassDocumentation(Class<?> clazz, int depth, List<ClassesOrder.EncapsulateDeclared> encapsulatedDeclarations) {
        final Function<Method, Path> methodToPath = m -> Paths.get(new DocumentationNamer(Paths.get("src", "test", "docs"), m).getApprovalFileName());
        // Trim because formatter add some line breaks (it may not add those line breaks)
        final Function<Path, String> includeWithOffset = path -> formatter.include(path.toString(), depth).trim();

        String content = encapsulatedDeclarations.stream()
                .map(encapsulateDeclared -> {
                    if (encapsulateDeclared instanceof ClassesOrder.EncapsulateDeclaredMethod) {
                        final Method encapsulatedMethod = ((ClassesOrder.EncapsulateDeclaredMethod) encapsulateDeclared).getEncapsulatedMethod();

                        final Path methodPath = methodToPath.apply(encapsulatedMethod);
                        return includeWithOffset.apply(methodPath);
                    }
                    if (encapsulateDeclared instanceof ClassesOrder.EncapsulateDeclaredClass) {
                        final Class<?> encapsulatedClass = ((ClassesOrder.EncapsulateDeclaredClass) encapsulateDeclared).getEncapsulatedClass();
                        return getMyClassDocumentation(encapsulatedClass, depth+1);
                    }
                    return "";
                }).collect(Collectors.joining("\n\n"));

        return formatter.paragraphSuite(
                getTitle(clazz, depth),
                getDescription(clazz),
                content);
    }

    public String getClassDocumentation(Class<?> clazz, List<Method> methods, Function<Method, Path> targetName, int depth) {

        final Map<Class<?>, List<Method>> methodsByClass = methods.stream()
                .collect(Collectors.groupingBy(method -> CodeExtractor.getFirstEnclosingClassBefore(method, clazz)));

        String content = (methodsByClass.size() == 1)
                ? includeMethods(methods, targetName, depth)
                : formatClasses(targetName, depth, methodsByClass);

        return formatter.paragraphSuite(
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

    protected String getDescription(Class<?> classToDocument) {
        return formatter.paragraphSuite(
                relatedClassDescription(classToDocument).orElse(""),
                CodeExtractor.getComment(classToDocument));
    }

    private Optional<String> relatedClassDescription(Class<?> fromClass) {
        return Optional.ofNullable(fromClass.getAnnotation(ClassToDocument.class))
                .map(ClassToDocument::clazz)
                .map(CodeExtractor::getComment);
    }

    public String getTitle(Class<?> clazz, int depth) {
        return new String(new char[depth]).replace("\0", "=") + " " + getTestClassTitle(clazz);
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

    private String includeMethods(List<Method> testMethods, Function<Method, Path> targetPathName, final int levelOffset) {
        final Stream<Path> pathToMethods = MethodsOrder.sort(testMethods)
                .map(targetPathName);

        return includeMethods(pathToMethods, levelOffset);
    }

    private String includeMethods(Stream<Path> pathToMethods, int levelOffset) {
        final Function<Path, String> includeWithOffset = path -> formatter.include(path.toString(), levelOffset).trim();
        // Trim because formatter add some line breaks (it may not add those line breaks)
        return pathToMethods
                .map(includeWithOffset)
                .collect(Collectors.joining("\n"));
    }

}
