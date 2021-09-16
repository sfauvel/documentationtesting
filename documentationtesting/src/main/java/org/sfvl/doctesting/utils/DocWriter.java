package org.sfvl.doctesting.utils;

import org.sfvl.doctesting.junitextension.ClassToDocument;
import org.sfvl.doctesting.writer.ClassDocumentation;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This object is used to store the text that need to be write to the final document.
 */
public class DocWriter {

    private StringBuffer sb = new StringBuffer();

    /**
     * Write a text to the output.
     */
    public void write(String... texts) {
        sb.append(Arrays.stream(texts).collect(Collectors.joining("\n")));
    }

    public String read() {
        return sb.toString();
    }

    public String formatOutput(Method testMethod) {
        final String title = methodAsTitle(testMethod.getName());
        return formatOutput(title, testMethod);
    }

    public String formatOutput(String title, Method testMethod) {
        return formatOutput(title, testMethod.getDeclaringClass(), testMethod);
    }

    public String formatOutput(String title, Class<?> classFile, Method testMethod) {
        return String.join("",
                defineDocPath(testMethod.getDeclaringClass()),
                "\n\n",
                formatAdocTitle(title, testMethod),
                CodeExtractor.getComment(classFile, testMethod).map(comment -> comment + "\n\n").orElse(""),
                read());
    }

    private String formatAdocTitle(String title, Method testMethod) {
        boolean isTitle = testMethod.getAnnotation(NoTitle.class) == null;
        return isTitle
                ? String.join("\n",
                String.format("[#%s]", titleId(testMethod)),
                "= " + formatTitle(title, testMethod),
                "", "")
                : "";
    }

    public String formatOutput(Class<?> clazz) {
        final ClassDocumentation classDocumentation = new ClassDocumentation() {
            protected Optional<String> relatedClassDescription(Class<?> fromClass) {
                return Optional.ofNullable(fromClass.getAnnotation(ClassToDocument.class))
                        .map(ClassToDocument::clazz)
                        .map(CodeExtractor::getComment);
            }
        };

        return String.join("\n",
                defineDocPath(clazz),
                "",
                classDocumentation.getClassDocumentation(clazz)
        );
    }

    public String defineDocPath(Class<?> clazz) {
        final String aPackage = clazz.getPackage().getName();
        final Path relativePathToRoot = Arrays.stream(aPackage.split("\\."))
                .map(__ -> Paths.get(".."))
                .reduce(Paths.get(""), Path::resolve);
        return defineDocPath(relativePathToRoot);
    }

    public String defineDocPath(Path relativePathToRoot) {
        return String.format("ifndef::%s[:%s: %s]", Config.DOC_PATH_TAG, Config.DOC_PATH_TAG, DocPath.toAsciiDoc(relativePathToRoot));
    }

    public String titleId(Method testMethod) {
        return String.format("%s_%s",
                testMethod.getDeclaringClass().getName()
                        .replace(".", "_")
                        .replace("$", "_"),
                testMethod.getName());
    }

    /**
     * Return the name to use as title from a test method.
     *
     * @param title
     * @param method
     * @return
     */
    public String formatTitle(String title, Method method) {
        return title;
    }

    private String methodAsTitle(String methodName) {
        String title = methodName.replace("_", " ");
        return title.substring(0, 1).toUpperCase() + title.substring(1);
    }

    public void reset() {
        sb = new StringBuffer();
    }
}
