package org.sfvl.doctesting.writer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sfvl.codeextraction.CodeExtractor;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.utils.ClassesOrder;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.OnePath;
import org.sfvl.doctesting.utils.PathProvider;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
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
    private final Predicate<Method> methodFilter;
    private final Function<OnePath, Path> onePathToPath;
    private final Predicate<Class> classFilter;

    public ClassDocumentation(Formatter formatter) {
        this(
                formatter,
                o -> Paths.get(o.filename()),
                m -> m.isAnnotationPresent(Test.class),
                c -> c.isAnnotationPresent(Nested.class)
        );
    }

    public ClassDocumentation(Formatter formatter,
                              Function<OnePath, Path> onePathToPath,
                              Predicate<Method> methodFilter,
                              Predicate<Class> classFilter) {
        this.formatter = formatter;
        this.onePathToPath = onePathToPath;
        this.methodFilter = methodFilter;
        this.classFilter = classFilter;
    }

    public String getClassDocumentation(Class<?> clazz) {
        return getClassDocumentation(clazz, 1);
    }

    public String getClassDocumentation(Class<?> clazz, int depth) {

        final ClassesOrder classesOrder = new ClassesOrder();

        final Stream<ClassesOrder.EncapsulateDeclared> declaredInOrder = classesOrder.getDeclaredInOrder(
                clazz,
                this.methodFilter,
                classFilter);

        return getClassDocumentation(clazz, depth, declaredInOrder.collect(Collectors.toList()));
    }

    protected String getClassDocumentation(Class<?> clazz, int depth, List<ClassesOrder.EncapsulateDeclared> encapsulatedDeclarations) {
        // Trim because formatter add some line breaks (it may not add those line breaks)
        final Function<Path, String> includeWithOffset = path -> formatter.include(path.toString(), depth).trim();

        String content = encapsulatedDeclarations.stream()
                .map(encapsulateDeclared -> {
                    if (encapsulateDeclared instanceof ClassesOrder.EncapsulateDeclaredMethod) {
                        final Method encapsulatedMethod = (Method) encapsulateDeclared.getEncapsulatedObject();

                        final Path receivedPath = new DocPath(encapsulatedMethod).received().path();
                        if (receivedPath.toFile().exists()) {
                            final Path methodReceivedPath = onePathToPath.apply(new DocPath(encapsulatedMethod).received());
                            return includeWithOffset.apply(methodReceivedPath);
                        }
                        final Path methodApprovedPath = onePathToPath.apply(new DocPath(encapsulatedMethod).approved());
                        return includeWithOffset.apply(methodApprovedPath);
                    }
                    if (encapsulateDeclared instanceof ClassesOrder.EncapsulateDeclaredClass) {
                        final Class<?> encapsulatedClass = (Class<?>) encapsulateDeclared.getEncapsulatedObject();
                        return getClassDocumentation(encapsulatedClass, depth + 1);
                    }
                    return "";
                }).collect(Collectors.joining("\n\n"));

        return formatter.paragraphSuite(
                getTitle(clazz, depth),
                getDescription(clazz),
                content);
    }

    protected String getDescription(Class<?> classToDocument) {
        return formatter.paragraphSuite(
                relatedClassDescription(classToDocument).orElse(""),
                getComment(classToDocument));
    }

    protected String getComment(Class<?> classToDocument) {
        return CodeExtractor.getComment(classToDocument).orElse("");
    }

    protected Optional<String> relatedClassDescription(Class<?> fromClass) {
        return Optional.empty();
    }

    public String getTitle(Class<?> clazz, int depth) {
        return new String(new char[depth]).replace("\0", "=") + " " + getTestClassTitle(clazz);
    }

    public String getTestClassTitle(Class<?> testClass) {
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

}
