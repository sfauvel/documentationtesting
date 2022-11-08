package org.sfvl.doctesting.writer;

import org.sfvl.codeextraction.CodeExtractor;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.utils.ClassToDocument;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.NoTitle;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This object is used to store the text that need to be written to the final document.
 */
public class DocWriter<F extends Formatter> {

    private StringBuffer sb = new StringBuffer();
    private F formatter;

    public DocWriter() {
        this((F) Config.FORMATTER);
    }

    public DocWriter(F formatter) {
        this.formatter = formatter;
    }

    public F getFormatter() {
        return formatter;
    }

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
                getComment(classFile, testMethod).map(comment -> comment + "\n\n").orElse(""),
                read());
    }

    protected Optional<String> getComment(Class<?> classFile, Method testMethod) {
        return CodeExtractor.getComment(classFile, testMethod);
    }

    protected String getComment(Class<?> clazz) {
        return CodeExtractor.getComment(clazz);
    }

    private String formatAdocTitle(String title, Method testMethod) {
        boolean isTitle = testMethod.getAnnotation(NoTitle.class) == null;

        return isTitle
                ? formatter.paragraph(
                formatter.blockId(titleId(testMethod)),
                formatter.title(1, formatTitle(title, testMethod)).trim()
        ) : "";
    }

    public String formatOutput(Class<?> clazz) {
        final ClassDocumentation classDocumentation =
                new MyClassDocumentation(this.formatter, this::titleId);

        return String.join("\n",
                defineDocPath(clazz),
                "",
                classDocumentation.getClassDocumentation(clazz)
        );
    }

    public String formatException(Throwable e, String title) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        return formatter.paragraph(formatter.bold(title),
                formatter.blockBuilder(Formatter.Block.CODE)
                        .content(sw.toString())
                        .build());
    }

    public String defineDocPath(Class<?> clazz) {
        return defineDocPath(relativePathToRoot(clazz));
    }

    public Path relativePathToRoot(Class<?> clazz) {
        final String aPackage = clazz.getPackage().getName();
        return Arrays.stream(aPackage.split("\\."))
                .map(__ -> Paths.get(".."))
                .reduce(Paths.get(""), Path::resolve);
    }

    public String defineDocPath(Path relativePathToRoot) {
        return String.format("ifndef::%s[:%s: %s]", Config.DOC_PATH_TAG, Config.DOC_PATH_TAG, DocPath.toAsciiDoc(relativePathToRoot));
    }

    public String titleId(Method testMethod) {
        return String.format("%s_%s",
                titleId(testMethod.getDeclaringClass()),
                testMethod.getName()).toLowerCase();
    }

    public String titleId(Class clazz) {
        return clazz.getName()
                .replace(".", "_")
                .replace("$", "_")
                .toLowerCase();
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

    private static class MyClassDocumentation extends ClassDocumentation {

        private Function<Class, String> titleId;

        public MyClassDocumentation(Formatter formatter, Function<Class, String> titleId) {
            super(formatter);
            this.titleId = titleId;
        }

        protected Optional<String> relatedClassDescription(Class<?> fromClass) {
            return Optional.ofNullable(fromClass.getAnnotation(ClassToDocument.class))
                    .map(ClassToDocument::clazz)
                    .map(this::getRelatedComment);
        }

        @Override
        public String getTitle(Class<?> clazz, int depth) {
            return String.join("\n",
                    formatter.blockId(titleId.apply(clazz)),
                    super.getTitle(clazz, depth));
        }

        protected String getRelatedComment(Class<?> clazz) {
            return CodeExtractor.getComment(clazz);
        }
    }
}
