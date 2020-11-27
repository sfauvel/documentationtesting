package org.sfvl.doctesting;

import com.github.javaparser.ParseProblemException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.FindLambdaMethod;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Extract information from code.
 */
@DisplayName(value = "CodeExtractor")
class CodeExtractorTest {

    class CodeExtractorWriter extends DocWriter {
        void writeInline(String... texts) {
            write(
                    "", "[.inline]",
                    "====",
                    String.join("\n", texts),
                    "====",
                    "");

        }
    }

    private final CodeExtractorWriter doc = new CodeExtractorWriter();
    @RegisterExtension
    ApprovalsExtension extension = new ApprovalsExtension(doc);

    @AfterEach
    public void addSyle() {
        doc.write("++++",
                "<style>",
                ".inline {",
                "   display: inline-block;",
                "   vertical-align: top;",
                "   margin-right: 2em;",
                "}",
                "#content {",
                "   max-width: unset;",
                "   padding-left: 5%;",
                "   padding-right: 5%;",
                "}",
                "</style>",
                "++++");

    }

    @Test
    public void extract_code_of_a_class(TestInfo testInfo) {

        // >>>
        String code = CodeExtractor.classSource(SimpleClass.class);
        // <<<

        doc.write(".How to extract code of a class",
                extractMarkedCode(testInfo),
                "");

        doc.writeInline(
                ".Source code to extract",
                includeSourceWithTag("classToExtract", SimpleClass.class)
        );

        doc.writeInline(
                ".Source code extracted",
                formatSourceCode(code)
        );
    }

    // tag::innerClassToExtract[]
    class SimpleInnerClass {
        public int simpleMethod() {
            return 0;
        }
    }
    // end::innerClassToExtract[]

    @Test
    public void extract_code_of_an_inner_class(TestInfo testInfo) {

        // >>>
        String code = CodeExtractor.classSource(CodeExtractorTest.SimpleInnerClass.class);
        // <<<

        doc.write(".How to extract code of an inner class",
                extractMarkedCode(testInfo),
                "");

        doc.writeInline(
                ".Source code to extract",
                includeSourceWithTag("innerClassToExtract", CodeExtractorTest.class)
        );

        doc.writeInline(
                ".Source code extracted",
                formatSourceCode(code)
        );
    }

    @Test
    public void extract_code_of_an_other_class_in_file(TestInfo testInfo) {
        {
            // >>>1
            String code = CodeExtractor.classSource(CodeExtractorTest.class, ClassNestedWithCommentToExtract.SubClassNestedWithCommentToExtract.class);
            // <<<1

            doc.write("It's not possible to extract code of a non public class because it's not possible to determine source file.",
                    "To be able to extract it, we have to explicitly give source file.", "", "");
            doc.write(".How to extract code of a non public class",
                    extractMarkedCode(testInfo, "1"),
                    "");

            doc.writeInline(
                    ".Source code to extract",
                    includeSourceWithTag("classNestedWithCommentToExtract", CodeExtractorTest.class)
            );

            doc.writeInline(
                    ".Source code extracted",
                    formatSourceCode(code)
            );
        }
        {
            try {
                // >>>2
                String code = CodeExtractor.classSource(ClassNestedWithCommentToExtract.SubClassNestedWithCommentToExtract.class);
                // <<<2

                doc.writeInline(
                        ".Source code extracted",
                        formatSourceCode(code)
                );
            } catch (ParseProblemException e) {
                doc.write("Non public class in a file could not be retrieve without giving main class of the file containing searching class.", "", "");

                doc.write(".This code does not work and throw an exception",
                        extractMarkedCode(testInfo, "2"),
                        "");

                doc.writeInline(
                        ".Exception thrown",
                        "++++",
                        e.getProblems().stream()
                                .map(c -> c.getCause().map(Throwable::toString).orElse("??"))
                                .collect(Collectors.joining("\n")),
                        "++++",
                        "", "");
            }
        }
    }

    @Test
    @DisplayName(value = "Extract code from method")
    public void extract_code_from_method(TestInfo testInfo) {

        // >>>
        String code = CodeExtractor.extractMethodBody(SimpleClass.class, "simpleMethod");
        // <<<

        doc.write(".How to extract code of a method",
                extractMarkedCode(testInfo),
                "");

        doc.writeInline(
                ".Source code from file",
                includeSourceWithTag("classToExtract", SimpleClass.class)
        );

        doc.writeInline(
                ".Source code extracted",
                formatSourceCode(code)
        );
    }

    /**
     * To extract a part of a method, you can write a comment with a specific value
     * that indicate beginining of the code to extract.
     * Another comment with a specific value indicate the end of the code.
     *
     * @param testInfo
     */
    @Test
    @DisplayName(value = "Extract a part of code from method")
    public void extract_part_of_code_from_method(TestInfo testInfo) {
        {
            // >>>1
            String code = CodeExtractor.extractPartOfMethod(SimpleClassA.class, "methodWithCodeToExtract", "");
            // <<<1

            doc.write(".How to extract part of a method code",
                    extractMarkedCode(testInfo, "1"),
                    "");

            doc.writeInline(
                    ".Source code from file",
                    formatSourceCode(CodeExtractor.classSource(SimpleClassA.class))
            );

            doc.writeInline(
                    ".Source code extracted",
                    formatSourceCode(code)
            );
        }
        {
            // >>>2
            final Method method = FindLambdaMethod.getMethod(SimpleClassA::methodWithCodeToExtract);
            String code = CodeExtractor.extractPartOfMethod(method);
            // <<<2

            doc.write(".How to extract part of a method code using Method object",
                    extractMarkedCode(testInfo, "2"),
                    "");

            doc.writeInline(
                    ".Source code extracted",
                    formatSourceCode(code)
            );
        }

        {
            // >>>3
            final Method method = FindLambdaMethod.getMethod(SimpleClassB::methodWithCodeToExtract);
            String codePart1 = CodeExtractor.extractPartOfMethod(method, "Part1");
            String codePart2 = CodeExtractor.extractPartOfMethod(method, "Part2");
            // <<<3

            doc.write("You can have several part identify by a text that you pass as argument " +
                    "to the extraction function", "", "");
            doc.write(".How to extract part of a method",
                    extractMarkedCode(testInfo, "3"),
                    "");

            doc.writeInline(
                    ".Source code from file",
                    formatSourceCode(CodeExtractor.classSource(SimpleClassB.class)));

            doc.writeInline(
                    ".Source code Part 1 extracted",
                    formatSourceCode(codePart1),
                    ".Source code Part 2 extracted",
                    formatSourceCode(codePart2));


        }
    }

    @Test
    @DisplayName(value = "Extract class comment")
    public void extract_class_comment(TestInfo testInfo) {
        {
            doc.write(
                    ".How to extract comment of a class",
                    extractMarkedCode(testInfo, "1"),
                    "");

            // >>>1
            final String comment = CodeExtractor.getComment(ClassWithCommentToExtract.class);
            // <<<1

            doc.writeInline(includeSourceWithTag("classWithComment"), "", "");

            formatCommentExtracted("Comment extracted from class",
                    comment);
        }
        {
            doc.write(
                    ".How to extract comment of a subclass",
                    extractMarkedCode(testInfo, "2"),
                    "");


            // >>>2
            final String comment = CodeExtractor.getComment(ClassNestedWithCommentToExtract.SubClassNestedWithCommentToExtract.class);
            // <<<2

            doc.writeInline(includeSourceWithTag("classNestedWithCommentToExtract"), "", "");

            formatCommentExtracted("Comment extracted from class",
                    comment);
        }
    }

    @Test
    @DisplayName(value = "Extract method comment")
    public void extract_method_comment(TestInfo testInfo) throws NoSuchMethodException {
        doc.writeInline(includeSourceWithTag("classWithComment"), "", "");

        {
            doc.write("How to extract comment of a method",
                    extractMarkedCode(testInfo, "1"),
                    "");

            // >>>1
            final Optional<String> comment = CodeExtractor.getComment(
                    ClassWithCommentToExtract.class,
                    "methodWithoutParameters"
            );
            // <<<1

            formatCommentExtracted("From method without arguments.",
                    comment.orElse(""));
        }
        {
            doc.write("How to extract comment of a method with parameters",
                    extractMarkedCode(testInfo, "2"),
                    "");

            // >>>2
            final Optional<String> comment = CodeExtractor.getComment(
                    ClassWithCommentToExtract.class,
                    "methodWithParameters",
                    CodeExtractor.getJavaClasses(int.class, String.class)
            );
            // <<<2

            formatCommentExtracted("From method with parameters.",
                    comment.orElse(""));
        }
        {
            doc.write("How to extract comment of a method using Method object",
                    extractMarkedCode(testInfo, "3"),
                    "");

            // >>>3
            final Method methodWithComment = ClassWithCommentToExtract.class.getMethod("methodWithoutParameters");
            final Optional<String> comment = CodeExtractor.getComment(methodWithComment);
            // <<<3

            formatCommentExtracted("From method",
                    comment.orElse(""));
        }

        {
            doc.write("How to extract comment of a method with parameters using Method object",
                    extractMarkedCode(testInfo, "4"),
                    "");

            // >>>4
            final Method methodWithComment = ClassWithCommentToExtract.class.getMethod("methodWithParameters", int.class, String.class);
            final Optional<String> comment = CodeExtractor.getComment(methodWithComment);
            // <<<4

            formatCommentExtracted("From method",
                    comment.orElse(""));
        }

        {
            doc.write("How to extract comment of a method of an inner class",
                    extractMarkedCode(testInfo, "5"),
                    "");


            // >>>5
            final Method methodWithComment = FindLambdaMethod.getMethod(ClassNestedWithCommentToExtract.SubClassNestedWithCommentToExtract::methodInSubClass);
            final Optional<String> comment = CodeExtractor.getComment(methodWithComment);
            // <<<5

            formatCommentExtracted("From method",
                    comment.orElse(""));
        }
    }

    @Test
    public void extract_enclosing_classes(TestInfo testInfo) throws NoSuchMethodException {
        {
            final Class<?> clazz = CodeExtractorTest.class;
            doc.write("[%autowidth]", "Class list of `" + clazz.getCanonicalName() + "` give :",
                    "",
                    "[%autowidth]",
                    "|====",
                    CodeExtractor.enclosingClasses(clazz).stream()
                            .map(Class::getSimpleName)
                            .collect(Collectors.joining(" | ", "| ", "")),
                    "|====",
                    "", ""
            );
        }
        {
            final Class<?> clazz = MyClass.MySubClass.ASubClassOfMySubClass.class;
            doc.write("Class list of `" + clazz.getCanonicalName() + "` give :",
                    "",
                    "[%autowidth]",
                    "|====",
                    CodeExtractor.enclosingClasses(clazz).stream()
                            .map(Class::getSimpleName)
                            .collect(Collectors.joining(" | ", "| ", "")),
                    "|====",
                    "", ""
            );
        }

    }

    @Test
    public void extract_first_enclosing_classes_before_another(TestInfo testInfo) throws NoSuchMethodException {

        BiConsumer<Class<?>, Class<?>> write_enclosing = (clazz, clazzBefore) -> {
            doc.write(String.format("First class of `%s` before `%s` give :",
                    clazz.getSimpleName(),
                    (clazzBefore == null ? "null" : clazzBefore.getSimpleName())
                    ),
                    "",
                    "`*" + CodeExtractor.getFirstEnclosingClassBefore(clazz, clazzBefore).getSimpleName() + "*`",
                    "", ""
            );
        };

        final Class<?> fromClass = MyClass.MySubClass.ASubClassOfMySubClass.class;
        doc.write("Extract from class `" + fromClass.getName()
                + "` using `" + "CodeExtractor.getFirstEnclosingClassBefore" + "`\n\n");

        write_enclosing.accept(
                fromClass,
                MyClass.MySubClass.ASubClassOfMySubClass.class);

        write_enclosing.accept(
                fromClass,
                MyClass.MySubClass.class);


        write_enclosing.accept(
                fromClass,
                MyClass.class);

        write_enclosing.accept(
                fromClass,
                null);
    }

    private String extractMarkedCode(TestInfo testInfo) {
        return formatSourceCode(CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get()));
    }

    private String extractMarkedCode(TestInfo testInfo, String suffix) {
        return formatSourceCode(CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get(), suffix));
    }

    private <T> String formatSourceCode(String source) {
        return String.join("\n",
                "[source, java, indent=0]",
                "----",
                source,
                "----");
    }

    public void formatCommentExtracted(String description, String commentExtracted) {
        doc.writeInline(
                "." + description,
                "----",
                commentExtracted,
                "----", "");
    }

    public String includeSourceWithTag(String tag) {
        final Class<? extends CodeExtractorTest> aClass = getClass();
        return includeSourceWithTag(tag, aClass);
    }

    public String includeSourceWithTag(String tag, Class<?> aClass) {
        return formatSourceCode(String.format("include::../../../../java/%s.java[tag=%s]",
                aClass.getName().replace(".", "/"),
                tag));
    }

}

@NotIncludeToDoc
// tag::classWithComment[]

/**
 * Comment of the class.
 */
class ClassWithCommentToExtract {

    /**
     * Comment of the method without parameters.
     */
    public void methodWithoutParameters() {

    }

    /**
     * Comment from the method with parameters.
     */
    public void methodWithParameters(int id, String name) {

    }

}
// end::classWithComment[]


@NotIncludeToDoc
// tag::classNestedWithCommentToExtract[]

/**
 * Comment of the class.
 */
class ClassNestedWithCommentToExtract {

    /**
     * Comment of the subclass.
     */
    class SubClassNestedWithCommentToExtract {
        /**
         * Method comment in an inner class.
         */
        @Test
        public void methodInSubClass() {
            System.out.println("My method");
        }
    }
}
// end::classNestedWithCommentToExtract[]