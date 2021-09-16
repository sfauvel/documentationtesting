package org.sfvl.doctesting.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.Formatter;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.ClassToDocument;
import org.sfvl.doctesting.junitextension.FindLambdaMethod;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.samples.*;
import org.sfvl.test_tools.OnlyRunProgrammatically;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@DisplayName("DocWriter")
class DocWriterTest {
    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    final AsciidocFormatter formatter = new AsciidocFormatter();

    private void write(String... texts) {
        doc.write(texts);
    }

    // >>>method_demo

    /**
     * This method shows what kind of output is provided.
     *
     * @param testInfo
     */
    @DisplayName("Method to make a demo")
    public void method_demo(TestInfo testInfo) {

    }
    // <<<method_demo

    // >>>simple_method
    public void simple_method() {

    }
    // <<<simple_method


    /**
     * DocWriter is just a buffer.
     * Everything wrote in DocWriter will be returned when asking for output.
     * The output is composed with a title, the comment of the method (without params).
     * An id is also added above the title to be able to apply a specific style in the chapter if needed.
     *
     * @param testInfo
     * @throws NoSuchMethodException
     */
    @Test
    @DisplayName("Usage")
    public void doc_writer_usage(TestInfo testInfo) throws NoSuchMethodException {

        // >>>
        final DocWriter doc = new DocWriter();
        doc.write(
                "Some text added to show DocWriter output.",
                "Multiple lines can be added."
        );

        final String output = doc.formatOutput(
                "My title",
                getClass().getMethod("simple_method")
        );
        // <<<

        DocWriterTest.doc.write(".Method used",
                formatter.sourceCode(CodeExtractor.methodSource(FindLambdaMethod.getMethod(DocWriterTest::simple_method))),
                "", "");

        DocWriterTest.doc.write(".DocWriter usage",
                formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                "", "");

        DocWriterTest.doc.write(formatter
                .blockBuilder(Formatter.Block.LITERAL)
                .title("Output provided")
                .content(output)
                .build());
    }

    /**
     * DocWriter is also used to format output of a test class.
     * In that case, we add a title and include all test files of the class.
     * What is written on DocWriter is not used in this case.
     *
     * @param testInfo
     * @throws NoSuchMethodException
     */
    @Test
    @DisplayName("Output with a class")
    public void doc_writer_with_a_class(TestInfo testInfo) throws NoSuchMethodException {

        // >>>
        final DocWriter doc = new DocWriter();

        final Class<?> clazz = MyTestWithTests.class;
        final String output = doc.formatOutput(clazz);
        // <<<

        DocWriterTest.doc.write(".Class used",
                formatter.sourceCode(extractAndCleanSource(clazz, clazz)),
                "", "");

        DocWriterTest.doc.write(".DocWriter usage",
                formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                "", "");

        DocWriterTest.doc.write(formatter
                .blockBuilder(Formatter.Block.LITERAL)
                .title("Output provided")
                .content(output.replaceAll("\\ninclude::", "\n\\\\include::"))
                .build());
    }

    /**
     * If you don't want the default title in the generated file, add @NoTitle annotation.
     * It can be useful when you want to include this file in another test
     * that have its own title for example.
     */
    @Test
    @DisplayName("Hide title")
    public void doc_writer_without_title(TestInfo testInfo) throws NoSuchMethodException {

        // >>>
        final DocWriter doc = new DocWriter();
        doc.write("Some text added to show DocWriter output.");
        final String output = doc.formatOutput(
                "This title will not be displayed",
                MyTestWithoutTitle.class.getMethod("my_method")
        );
        // <<<

        DocWriterTest.doc.write(".Test with NoTitle annotation",
                formatter.sourceCode(CodeExtractor.classSource(MyTestWithoutTitle.class)),
                "", "");

        DocWriterTest.doc.write(".DocWriter usage",
                formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                "", "");

        DocWriterTest.doc.write(formatter
                .blockBuilder(Formatter.Block.LITERAL)
                .title("Output provided")
                .content(output)
                .build());
    }

    @Test
    @DisplayName("Format title")
    public void format_title() {
        final DocWriter docWriter = new DocWriter();
        try {
            // >>>with_title
            Method testMethod = DocWriterTest.class.getMethod("format_title");
            final String output = docWriter.formatOutput("My title", testMethod);
            // <<<with_title

            doc.write(
                    "When a title is specified, it is used as title.",
                    formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod("with_title")),
                    formatter.blockBuilder("----")
                            .content(output)
                            .build(),
                    ""
            );
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        try {
            // >>>without_title
            Method testMethod = DocWriterTest.class.getMethod("format_title");
            final String method_output = docWriter.formatOutput(testMethod);

            final String class_output = docWriter.formatOutput(MyTest.class);
            // <<<without_title

            doc.write(
                    "When a title is not specified, title comes from the method name or the class name after some formatting",
                    " (remove '_', uppercase first letter).",
                    formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod("without_title")),
                    formatter.blockBuilder("----")
                            .title("Format title from method name")
                            .escapeSpecialKeywords()
                            .content(method_output)
                            .build(),
                    formatter.blockBuilder("----")
                            .title("Format title from class name")
                            .escapeSpecialKeywords()
                            .content(class_output)
                            .build(),
                    ""
            );
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public void test_method_with_test_info(TestInfo testInfo) {
    }

    /**
     * Comment used as method description.
     */
    public void myMethod() {

    }

    /**
     * A description can be added after the title using the Javadoc.
     * It can be done with the method javadoc or the class javadoc.
     *
     * @param testInfo
     * @throws NoSuchMethodException
     */
    @Test
    @DisplayName("Add a description")
    public void add_description_using_comment(TestInfo testInfo) throws NoSuchMethodException {

        // >>>
        final DocWriter writer = new DocWriter();

        final String method_output = writer.formatOutput(
                "My title",
                MyTestWithComment.class.getMethod("test_A")
        );

        final String class_output = writer.formatOutput(MyTestWithComment.class);
        // <<<

        doc.write(".Class used",
                formatter.sourceCode(extractAndCleanSource(MyTestWithComment.class, MyTestWithComment.class)),
                "", "");

        doc.write(".DocWriter usage",
                formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod()),
                "", "");

        doc.write(formatter
                        .blockBuilder(Formatter.Block.LITERAL)
                        .title("Output provided with method")
                        .escapeSpecialKeywords()
                        .content(method_output)
                        .build(),
                formatter
                        .blockBuilder(Formatter.Block.LITERAL)
                        .title("Output provided with class")
                        .escapeSpecialKeywords()
                        .content(class_output)
                        .build(),
                "");

        doc.write("If we want to add description of an other class (class under test for example),",
        String.format("we can use `%s` to define the class containing the description we want.", ClassToDocument.class.getSimpleName()),
                "It can be combine with the description on the test class.",
                "");

        doc.write("", ".Test class used",
                formatter.sourceCode(extractAndCleanSource(MyTestWithClassToDocument.class, MyTestWithClassToDocument.class)),
                "", "");

        doc.write(".Class under test with description",
                formatter.sourceCode(extractAndCleanSource(ClassUnderTest.class, ClassUnderTest.class)),
                "", "");

        doc.write(formatter
                .blockBuilder(Formatter.Block.LITERAL)
                .title("Output provided")
                .escapeSpecialKeywords()
                .content(writer.formatOutput(MyTestWithClassToDocument.class))
                .build());

    }

    private String extractAndCleanSource(Class<?> classToIdentifySourceClass, Class<?> testClass) {
//        final Class<?> mainFileClass = new ClassFinder().getMainFileClass(classToIdentifySourceClass);
//        final Path path = new DocPath(mainFileClass).test().path();
//        final String javaSource = CodeExtractor.extractPartOfFile(path, tag)
        final String javaSource = CodeExtractor.classSource(classToIdentifySourceClass)
                .replaceAll("(^|\n)@" + NotIncludeToDoc.class.getSimpleName(), "")
                .replaceAll("(^|\n)@" + OnlyRunProgrammatically.class.getSimpleName(), "");
//        return formatter.sourceCode(javaSource.trim()).trim();
        return javaSource;
    }


    public void writeFormatOutput(String displayName, Method testMethod) {
        final DocWriter docWriterForTest = new DocWriter();
        final String output = docWriterForTest.formatOutput(displayName, testMethod)
                .replaceAll("(^)\\[#", "//[#")
                .replaceAll("\\n\\[#", "\n//[#");

        doc.write(String.format("Calling formatOutput with DisplayName=\"%s\" and Method=%s provides",
                displayName, testMethod.getName()), "");

        doc.write("====", output, "====", "");
    }

    public String includeSourceWithTag(String tag) {
        final int nb_folder = this.getClass().getPackage().getName().split("\\.").length + 1;
        final String rootPath = IntStream.range(0, nb_folder)
                .mapToObj(i -> "..")
                .collect(Collectors.joining("/"));

        final String filename = String.format("%s/java/%s.java", rootPath, getClass().getName().replace(".", "/"));
        return String.join("\n",
                "[source, java, indent=0]",
                "----",
                formatter.include_with_tag(filename, tag),
                "----");
    }

    public void simple_method_to_format_title() {

    }

    @Test
    @DisplayName("Formatted title")
    public void title(TestInfo testinfo) throws NoSuchMethodException {
        final Method method1 = DocWriter.class.getDeclaredMethod("formatTitle", String.class, Method.class);
        CodeExtractor.getComment(method1)
                .ifPresent(doc -> write(doc + "\n"));

        final Method method = FindLambdaMethod.getMethod(DocWriterTest::simple_method_to_format_title);
        final Method method_with_test_info = FindLambdaMethod.getMethod(DocWriterTest::test_method_with_test_info);

        List<List<? extends Object>> table = new ArrayList<>();
        table.add(Arrays.asList("Display name", "Method name", "Title"));
        table.add(getFormatTitleLine("Get display name", method));
        table.add(getFormatTitleLine("simple_method_to_format_title()", method));
        table.add(getFormatTitleLine("display_name_is_not_method_name()", method));
        table.add(getFormatTitleLine("test_method_with_test_info(TestInfo)", method_with_test_info));

        write(new AsciidocFormatter().tableWithHeader(table));
    }

    public List<String> getFormatTitleLine(TestInfo testInfo) {
        return getFormatTitleLine(
                testInfo.getDisplayName(),
                testInfo.getTestMethod().get()
        );
    }

    public List<String> getFormatTitleLine(String displayName, Method method) {
        return Arrays.asList(
                displayName,
                method.getName(),
                new DocWriter().formatTitle(displayName, method)
        );
    }
//
//    @NotIncludeToDoc
//// tag::MyTestWithComment[]
//    /**
//     * My comment MyTestWithComment.
//     */
//    private static class MyTestWithComment {
//        private static final DocWriter docWriter = new DocWriter();
//        @RegisterExtension
//        static ApprovalsExtension extension = new ApprovalsExtension(docWriter);
//
//        /**
//         * To decribe a method, you can add a comment.
//         * It will be added under title.
//         */
//        @Test
//        public void testA() {
//            docWriter.write("In my *test*");
//        }
//
//    }
//// end::MyTestWithComment[]
}