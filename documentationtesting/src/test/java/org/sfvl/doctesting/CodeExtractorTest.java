package org.sfvl.doctesting;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.DocWriter;
import org.sfvl.doctesting.junitextension.FindLambdaMethod;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Extract information from code.
 */
@DisplayName(value = "CodeExtractor")
class CodeExtractorTest {

    private final DocWriter docWriter = new DocWriter();
    @RegisterExtension
    ApprovalsExtension extension = new ApprovalsExtension(docWriter);

    @AfterEach
    public void addSyle() {
        docWriter.write("++++",
                "<style>",
                ".inline {",
                "   display: inline-block;",
                "   vertical-align: top;",
                "   margin-right: 2em;",
                "}",
                "</style>",
                "++++");

    }

    @Test
    @DisplayName(value = "Extract code from class")
    public void extract_code_from_class(TestInfo testInfo) {

        // >>>
        String code = CodeExtractor.getCode(SimpleClass.class);
        // <<<

        docWriter.write(".How to extract code of a class",
//                extractSourceCode(CodeExtractorTest::extract_code_from_class),
                extractSourceCode(testInfo),
                "");

        docWriter.write("[.inline]",
                ".Source code to extract",
                includeSourceWithTag("classToExtract", SimpleClass.class),
                "", "");

        docWriter.write("[.inline]",
                ".Source code extracted",
                formatSourceCode(code)
                , "");
    }

    @Test
    @DisplayName(value = "Extract code from method")
    public void extract_code_from_method(TestInfo testInfo) {

        // >>>
        String code = CodeExtractor.extractMethodBody(SimpleClass.class, "simpleMethod");
        // <<<

        docWriter.write(".How to extract code of a method",
                extractSourceCode(testInfo),
                "");

        docWriter.write("[.inline]",
                ".Source code from file",
                includeSourceWithTag("classToExtract", SimpleClass.class),
                "", "");

        docWriter.write("[.inline]",
                ".Source code extracted",
                formatSourceCode(code),
                "");
    }

    @Test
    @DisplayName(value = "Extract class comment")
    public void extract_class_comment(TestInfo testInfo) {
        docWriter.write(".How to extract comment of a class",
                extractSourceCode(testInfo),
                "");

        // >>>
        final String comment = CodeExtractor.getComment(ClassWithCommentToExtract.class);
        // <<<

        docWriter.write(includeSourceWithTag("classWithComment"), "", "");

        format_comment_extracted("Comment extracted from class",
                comment);
    }

    @Test
    @DisplayName(value = "Extract method comment")
    public void extract_method_comment(TestInfo testInfo) throws NoSuchMethodException {
        docWriter.write(includeSourceWithTag("classWithComment"), "", "");

        {
            docWriter.write("How to extract comment of a method",
                    extractSourceCode(testInfo, "1"),
                    "");

            // >>>1
            final Optional<String> comment = CodeExtractor.getComment(
                    ClassWithCommentToExtract.class,
                    "methodWithoutParameters"
            );
            // <<<1

            format_comment_extracted("From method without arguments.",
                    comment.orElse(""));
        }
        {
            docWriter.write("How to extract comment of a method with parameters",
                    extractSourceCode(testInfo, "2"),
                    "");

            // >>>2
            final Optional<String> comment = CodeExtractor.getComment(
                    ClassWithCommentToExtract.class,
                    "methodWithParameters",
                    CodeExtractor.getJavaClasses(int.class, String.class)
            );
            // <<<2

            format_comment_extracted("From method with parameters.",
                    comment.orElse(""));
        }
        {
            docWriter.write("How to extract comment of a method using Method object",
                    extractSourceCode(testInfo, "3"),
                    "");

            // >>>3
            final Method methodWithComment = ClassWithCommentToExtract.class.getMethod("methodWithoutParameters");
            final Optional<String> comment = CodeExtractor.getComment(methodWithComment);
            // <<<3

            format_comment_extracted("From method",
                    comment.orElse(""));
        }
    }

    private <T> String extractSourceCode(FindLambdaMethod.SerializableConsumer<T> lambda) {
        return formatSourceCode(extractCodeBetween(lambda));
    }

    private String extractSourceCode(TestInfo testInfo) {
        return formatSourceCode(extractCodeBetween(testInfo.getTestMethod().get()));
    }
    private String extractSourceCode(TestInfo testInfo, String suffix) {
        return formatSourceCode(extractCodeBetween(testInfo.getTestMethod().get(), suffix));
    }

    public <T> String extractCodeBetween(FindLambdaMethod.SerializableConsumer<T> lambda) {

        final Method method = FindLambdaMethod.getMethod(lambda);
        return extractCodeBetween(method);
    }

    public String extractCodeBetween(Method method) {
        return extractCodeBetween(method, "");
    }

    public String extractCodeBetween(Method method, String suffix) {
        final String source = CodeExtractor.extractMethodBody(method.getDeclaringClass(), method.getName());
//        final String s = CodeExtractor.extractMethodBody(aClass,//CodeExtractorTest.class,
//                FindLambdaMethod.getName(extract_code_from_class));
        return extractCodeBetween(source, ">>>"+ suffix, "<<<"+ suffix);
    }

    public String extractCodeBetween(String source, String begin, String end) {
        // Not compatible with JDK1.8
//                return s.lines()
//                .dropWhile(line -> line.contains("//"+ " tag::"))
//                .takeWhile(line -> line.contains("//"+ " end::"))
//                .collect(Collectors.joining("\n"));

        StringBuffer buffer = new StringBuffer();
        boolean inTag = false;
        for (String s1 : source.split("\n")) {

            if (s1.contains("// " + end)) {
                inTag = false;
            }
            if (inTag) {
                buffer.append(s1 + "\n");
            }
            if (s1.contains("// " + begin)) {
                inTag = true;
            }
        }
        return buffer.toString();
    }

    private <T> String formatSourceCode(String source) {
        return String.join("\n",
                "[source, java, indent=0]",
                "----",
                source,
                "----");
    }

    public void format_comment_extracted(String description, String commentExtracted) {
        docWriter.write(
                "." + description,
                "----",
                commentExtracted,
                "----", "", "");
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
