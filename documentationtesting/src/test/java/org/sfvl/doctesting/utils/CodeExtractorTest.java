package org.sfvl.doctesting.utils;

import com.github.javaparser.ParseProblemException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.FindLambdaMethod;
import org.sfvl.doctesting.sample.ClassWithCommentToExtract;
import org.sfvl.doctesting.sample.ClassWithMethodToExtract;
import org.sfvl.doctesting.sample.MyClass;
import org.sfvl.doctesting.sample.SimpleClass;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * It is often useful to retrieve information from the source code itself.
 * This class provides utilities to extract pieces of code or comments.
 */
@DisplayName(value = "CodeExtractor")
class CodeExtractorTest {

    static class CodeExtractorWriter extends DocWriter {
        void writeInline(String... texts) {
            write(
                    "", "[.inline]",
                    "====",
                    String.join("\n", texts),
                    "====",
                    "");

        }
    }

    private static final CodeExtractorWriter doc = new CodeExtractorWriter();

    @RegisterExtension
    static ApprovalsExtension extension = new ApprovalsExtension(doc);

    @AfterEach
    public void addSyle(TestInfo testInfo) {
        final String title = "_" + doc.formatTitle(testInfo.getDisplayName(), testInfo.getTestMethod().get())
                .replaceAll(" ", "_")
                .toLowerCase();
        // Id automatically added when toc is activate but not on H1 title so we add one.
        doc.write("++++",
                "<style>",
                "#" + doc.titleId(testInfo.getTestMethod().get()) + " ~ .inline {",
                "   display: inline-block;",
                "   vertical-align: top;",
                "   margin-right: 2em;",
                "}",
                "</style>",
                "++++");

    }

    // tag::innerClassToExtract[]
    class SimpleInnerClass {
        public int simpleMethod() {
            return 0;
        }
    }
    // end::innerClassToExtract[]

    @Nested
    @DisplayName(value = "Extract code")
    class ExtractCode {

        @Test
        public void extract_code_of_a_class(TestInfo testInfo) {

            // >>>
            Class<?> classToExtract = SimpleClass.class;
            String code = CodeExtractor.classSource(classToExtract);
            // <<<

            doc.write(".How to extract code of a class",
                    extractMarkedCode(testInfo),
                    "");

            doc.writeInline(
                    ".Source code to extract",
                    includeSourceWithTag("classToExtract", classToExtract)
            );

            doc.writeInline(
                    ".Source code extracted",
                    formatSourceCode(code)
            );
        }

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
        @DisplayName(value = "Extract code of a non public class in file")
        public void extract_code_of_an_other_class_in_file(TestInfo testInfo) {
            {
                // >>>1
                String code = CodeExtractor.classSource(CodeExtractorTest.class,
                        ClassNestedWithCommentToExtract.SubClassNestedWithCommentToExtract.class);
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
                    String code = CodeExtractor.classSource(
                            ClassNestedWithCommentToExtract.SubClassNestedWithCommentToExtract.class);
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
            doc.write("Method source code can be retrived from his method object or from his class and his method name.",
                    "It's also possible to retrieve only the method body.",
                    "", "");

            String codeWithClassAndMethodName;
            {
                // >>>1
                String code = CodeExtractor.methodSource(SimpleClass.class, "simpleMethod");
                // <<<1
                codeWithClassAndMethodName = code;
            }
            String codeWithMethod;
            {
                // >>>2
                Method method = FindLambdaMethod.getMethod(SimpleClass::simpleMethod);
                String code = CodeExtractor.methodSource(method);
                // <<<2
                codeWithMethod = code;
            }

            doc.write(".How to extract code of a method",
                    extractMarkedCode(testInfo, "1"),
                    "");

            if (codeWithMethod.equals(codeWithClassAndMethodName)) {
                doc.write("or",
                        extractMarkedCode(testInfo, "2"),
                        "");
            }

            doc.writeInline(
                    ".Source code from file",
                    includeSourceWithTag("classToExtract", SimpleClass.class)
            );

            doc.writeInline(
                    ".Source code extracted",
                    formatSourceCode(codeWithClassAndMethodName)
            );

            if (!codeWithMethod.equals(codeWithClassAndMethodName)) {


                doc.write(".How to extract code of a method using method Object",
                        extractMarkedCode(testInfo, "2"),
                        "");

                doc.writeInline(
                        ".Source code from file",
                        includeSourceWithTag("classToExtract", SimpleClass.class)
                );

                doc.writeInline(
                        ".Source code extracted using method",
                        formatSourceCode(codeWithMethod)
                );
            }

            {
                // >>>3
                String code = CodeExtractor.extractMethodBody(SimpleClass.class, "simpleMethod");
                // <<<3

                doc.write(".How to extract body of a method",
                        extractMarkedCode(testInfo, "3"),
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
                String codeFromMethod;
                {
                    // >>>1
                    Method method = FindLambdaMethod.getMethod(ClassWithMethodToExtract::methodWithOnePartToExtract);
                    String code = CodeExtractor.extractPartOfMethod(method);
                    // <<<1
                    codeFromMethod = code;
                }
                String codeFromClassAndMethodName;
                {
                    // >>>2
                    String code = CodeExtractor.extractPartOfMethod(ClassWithMethodToExtract.class, "methodWithOnePartToExtract");
                    // <<<2
                    codeFromClassAndMethodName = code;
                }
                doc.write(".How to extract part of a method code",
                        extractMarkedCode(testInfo, "1"),
                        "");

                if (codeFromMethod.equals(codeFromClassAndMethodName)) {
                    doc.write("or",
                            extractMarkedCode(testInfo, "2"),
                            "");
                }

                Method method = FindLambdaMethod.getMethod(ClassWithMethodToExtract::methodWithOnePartToExtract);
                doc.writeInline(
                        ".Source code from file",
                        formatSourceCode(CodeExtractor.methodSource(method))
                );

                doc.writeInline(
                        ".Source code extracted",
                        formatSourceCode(codeFromMethod)
                );

                if (!codeFromMethod.equals(codeFromClassAndMethodName)) {
                    doc.write(".How to extract code of a method using class and method name",
                            extractMarkedCode(testInfo, "2"),
                            "");

                    doc.writeInline(
                            ".Source code from file",
                            formatSourceCode(CodeExtractor.methodSource(method))
                    );

                    doc.writeInline(
                            ".Source code extracted using class and method name",
                            formatSourceCode(codeFromClassAndMethodName)
                    );
                }
            }
            {
                // >>>3
                Method method = FindLambdaMethod.getMethod(ClassWithMethodToExtract::methodWithSeveralPartsToExtract);
                String codePart1 = CodeExtractor.extractPartOfMethod(method, "Part1");
                String codePart2 = CodeExtractor.extractPartOfMethod(method, "Part2");
                // <<<3

                doc.write("You can have several part identify by a text that you pass as argument " +
                                "to the function extracting the code.",
                        "You can have several part identified by the same text.",
                        "In that case, all parts matching the text will be returned.",
                        "", "");
                doc.write(".How to extract part of a method",
                        extractMarkedCode(testInfo, "3"),
                        "");

                doc.writeInline(
                        ".Source code from file",
                        formatSourceCode(CodeExtractor.methodSource(method)));

                doc.writeInline(
                        ".Source code Part 1 extracted",
                        formatSourceCode(codePart1),
                        ".Source code Part 2 extracted",
                        formatSourceCode(codePart2));

            }
        }

        public String method_with_code_to_extract() {
            // >>>
            String value = "some text";
            // <<<

            return CodeExtractor.extractPartOfCurrentMethod();
        }

        public String method_with_code_to_extract_with_tag() {
            // >>>1
            String value1 = "some text";
            // <<<1

            // >>>2
            String value2 = "code to extract";
            // <<<2

            return CodeExtractor.extractPartOfCurrentMethod("2");
        }

        @Test
        public void extract_a_part_of_code_from_the_current_method(TestInfo testInfo) {
            {
                {
                    // >>>12
                    String code = CodeExtractor.extractPartOfCurrentMethod();
                    // <<<12
                }

                String code =
                        // >>>99
                        method_with_code_to_extract
                                // <<<99
                                        ();
                String codeToExtract = CodeExtractor.methodSource(this.getClass(), CodeExtractor.extractPartOfCurrentMethod("99").trim());

                doc.write(".How to extract code from the current method",
                        extractMarkedCode(testInfo, "12"),
                        "");

                doc.writeInline(
                        ".Source code from file",
                        formatSourceCode(codeToExtract)
                );

                doc.writeInline(
                        ".Source code extracted from the current method",
                        formatSourceCode(code)
                );
            }
            {
                {
                    // >>>2
                    String code = CodeExtractor.extractPartOfCurrentMethod("2");
                    // <<<2
                }

                String code =
                        // >>>98
                        method_with_code_to_extract_with_tag
                                // <<<98
                                        ();
                String codeToExtract = CodeExtractor.methodSource(this.getClass(), CodeExtractor.extractPartOfCurrentMethod("98").trim());

                doc.write(".How to extract code from the current method using a tag",
                        extractMarkedCode(testInfo, "2"),
                        "");

                doc.writeInline(
                        ".Source code from file",
                        formatSourceCode(codeToExtract)
                );

                doc.writeInline(
                        ".Source code extracted from the current method",
                        formatSourceCode(code)
                );
            }
        }

        /**
         * We shows here some technical cases.
         */
        @Nested
        class ExtractPartOfCode {

            /**
             * Tag (MyCode) could be a subpart of another one (**MyCode**Before or **MyCode**After).
             */
            @Test
            public void tag_with_same_beginning_of_another_tag(TestInfo testInfo) {
                final Method testMethod = testInfo.getTestMethod().get();

                // >>>SourceCode
                // >>>MyCodeBefore
                String partBefore = "Part before MyCode";
                // <<<MyCodeBefore

                // >>>MyCode
                String partMyCode = "Part MyCode";
                // <<<MyCode

                // >>>MyCodeAfter
                String partAfter = "Part after MyCode";
                // <<<MyCodeAfter
                // <<<SourceCode

                doc.write(".Source code with extractor tags",
                        formatSourceCode(CodeExtractor.extractPartOfMethod(testMethod, "SourceCode")));

                doc.writeInline(
                        ".Source code part MyCodeBefore extracted",
                        formatSourceCode(CodeExtractor.extractPartOfMethod(testMethod, "MyCodeBefore")),
                        ".Source code part MyCodeAfter extracted",
                        formatSourceCode(CodeExtractor.extractPartOfMethod(testMethod, "MyCodeAfter")),
                        ".Source code part MyCode extracted",
                        formatSourceCode(CodeExtractor.extractPartOfMethod(testMethod, "MyCode")));

            }

            /**
             * Tag inside another one can be a subpart (MyCode)  of the global one (**MyCode**Global) .
             */
            @Test
            public void tag_beginning_with_same_outer_tag_name(TestInfo testInfo) {
                final Method testMethod = testInfo.getTestMethod().get();

                // >>>SourceCode
                // >>>MyCodeGlobal
                String partGlobalBefore = "Part global before MyCode";

                // >>>MyCode
                String partMyCode = "Part MyCode";
                // <<<MyCode

                String partGlobalAfter = "Part global after MyCode";
                // <<<MyCodeGlobal
                // <<<SourceCode

                doc.write(".Source code with extractor tags",
                        formatSourceCode(CodeExtractor.extractPartOfMethod(testMethod, "SourceCode")));

                doc.writeInline(
                        ".Source code part MyCodeInside extracted",
                        formatSourceCode(CodeExtractor.extractPartOfMethod(testMethod, "MyCode")),
                        ".Source code part MyCodeInsideAround extracted",
                        formatSourceCode(CodeExtractor.extractPartOfMethod(testMethod, "MyCodeGlobal")));
            }

            /**
             * Tag inside (**MyCodeGlobal**Inside) another one can be an extension of an outside tag (MyCodeGlobal).
             */
            @Test
            public void tag_beginning_with_same_inner_tag_name(TestInfo testInfo) {
                final Method testMethod = testInfo.getTestMethod().get();

                // >>>SourceCode
                // >>>MyCodeGlobal
                String partGlobalBefore = "Part global before MyCode";

                // >>>MyCodeGlobalInside
                String partInside = "Part MyCode";
                // <<<MyCodeGlobalInside

                String partGlobalAfter = "Part global after MyCode";
                // <<<MyCodeGlobal
                // <<<SourceCode

                doc.write(".Source code with extractor tags",
                        formatSourceCode(CodeExtractor.extractPartOfMethod(testMethod, "SourceCode")));

                doc.writeInline(
                        ".Source code part MyCodeEnclosed extracted",
                        formatSourceCode(CodeExtractor.extractPartOfMethod(testMethod, "MyCodeGlobalInside")),
                        ".Source code part MyCodeEnclosedInside extracted",
                        formatSourceCode(CodeExtractor.extractPartOfMethod(testMethod, "MyCodeGlobal")));
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
    }
    // tag::NestedClass[]

    /**
     * Comment of the nested class of CodeExtractorTest.
     */
    @NotIncludeToDoc
    class NestedClass {
        /**
         * Method comment in an inner class.
         */
        @Test
        public void methodInSubClass() {
            System.out.println("My method");
        }
    }
    // end::NestedClass[]

    @Nested
    @DisplayName(value = "Extract comment")
    class ExtractComment {

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

                doc.writeInline(includeSourceWithTag("ClassWithCommentToExtract", ClassWithCommentToExtract.class), "", "");

                formatCommentExtracted("Comment extracted from class",
                        comment);
            }
            {
                doc.write(
                        ".How to extract comment of a nested class",
                        extractMarkedCode(testInfo, "2"),
                        "");

                // >>>2
                final String comment = CodeExtractor.getComment(CodeExtractorTest.NestedClass.class);
                // <<<2

                doc.writeInline(includeSourceWithTag(NestedClass.class.getSimpleName()), "", "");

                formatCommentExtracted("Comment extracted from class",
                        comment);
            }
            {
                doc.write(
                        ".How to extract comment of class that is not the main class of his file.",
                        extractMarkedCode(testInfo, "3"),
                        "");


                // >>>3
                final String comment = CodeExtractor.getComment(ClassNestedWithCommentToExtract.class);
                // <<<3

                doc.writeInline(includeSourceWithTag("classNestedWithCommentToExtract"), "", "");

                formatCommentExtracted("Comment extracted from class",
                        comment);
            }
        }

        @Test
        @DisplayName(value = "Extract method comment")
        public void extract_method_comment(TestInfo testInfo) throws NoSuchMethodException {
            doc.writeInline(includeSourceWithTag(ClassWithCommentToExtract.class.getSimpleName(), ClassWithCommentToExtract.class), "", "");

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
        final DocPath docPath = new DocPath(aClass);
        return formatSourceCode(String.format("include::%s[tag=%s]",
                docPath.test().from(this.getClass()),
                tag));
    }

}

// tag::classNestedWithCommentToExtract[]
@NotIncludeToDoc
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