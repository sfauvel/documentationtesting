package org.sfvl.doctesting.utils;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.AsciidocFormatterTest;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.FindLambdaMethod;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.samples.MyTest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

    /**
     * This method shows what kind of output is provided.
     *
     * @param testInfo
     */
    @DisplayName("Method to make a demo")
    public void method_demo(TestInfo testInfo) {

    }

    /**
     * DocWriter is just a buffer.
     * Everything wrote in DocWriter will be returned when asking for output.
     * By default, a title is added to the output.
     *
     * @param testInfo
     * @throws NoSuchMethodException
     */
    @Test
    @DisplayName("DocWriter usage")
    public void doc_writer_usage(TestInfo testInfo) throws NoSuchMethodException {

        // >>>
        final DocWriter doc = new DocWriter();
        doc.write("Some text added to show DocWriter output.");
        final String output = doc.formatOutput(
                "My title",
                getClass().getMethod("method_demo", TestInfo.class)
        );
        // <<<

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
     * What is wrote on DocWriter is not used in this case.
     *
     * @param testInfo
     * @throws NoSuchMethodException
     */
    @Test
    @DisplayName("DocWriter of a class")
    public void doc_writer_with_a_class(TestInfo testInfo) throws NoSuchMethodException {

        // >>>
        final DocWriter doc = new DocWriter();
        doc.write("Some text added to show DocWriter output.");
        final String output = doc.formatOutput(MyTest.class);
        // <<<

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
     * It can be useful when you want to include this file in another test for example.
     */
    @Test
    @DisplayName("DocWriter without title")
    public void doc_writer_without_title(TestInfo testInfo) throws NoSuchMethodException {

        // >>>test_class
        class MyTest {
            @Test
            @NoTitle
            public void my_method() {
                // my doc generation
            }
        }
        // <<<test_class

        // >>>
        final DocWriter doc = new DocWriter();
        doc.write("Some text added to show DocWriter output.");
        final String output = doc.formatOutput(
                "Do not display this title",
                MyTest.class.getMethod("my_method")
        );
        // <<<

        DocWriterTest.doc.write(".Test with NoTitle annotation",
                formatter.sourceCode(CodeExtractor.extractPartOfCurrentMethod("test_class")),
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
        final Method testMethod = FindLambdaMethod.getMethod(DocWriterTest::format_title);
        final String name = testMethod.getName();

        {
            doc.write("When display name is different from test method name, displayName is used as title.", "", "");

            writeFormatOutput("My title", testMethod);
        }

        {
            doc.write("When display name is the same as test method name, name is reformatted.", "", "");

            writeFormatOutput(testMethod.getName() + "()", testMethod);
        }

        {
            doc.write("Test method could have TestInfo parameter.", "", "");

            final Method testMethodWithTestInfo = FindLambdaMethod.getMethod(DocWriterTest::test_method_with_test_info);
            writeFormatOutput(testMethodWithTestInfo.getName() + "(TestInfo)", testMethodWithTestInfo);
        }
    }

    public void test_method_with_test_info(TestInfo testInfo)  {
    }

    /**
     * Comment used as method description.
     */
    public void myMethod() {

    }

    @Test
    @DisplayName("Add description")
    public void add_description_using_comment(TestInfo testInfo) throws NoSuchMethodException {
        final String name = FindLambdaMethod.getName(MyTestWithComment::testA);
        final Method testMethod = MyTestWithComment.class.getMethod(name);

        doc.write("When test method had a comment, it's written after title.", "", "");

        final DocWriter docWriterForTest = new DocWriter();
        final String output = docWriterForTest.formatOutput(name + "()", testMethod);

        doc.write(".Test example with comment on method",
  //              CodeExtractor.classSource(MyTestWithComment.class),
                includeSourceWithTag(MyTestWithComment.class.getSimpleName()),
                "", "");

        doc.write("****", output, "****", "");
    }

    public void writeFormatOutput(String displayName, Method testMethod) {
        final DocWriter docWriterForTest = new DocWriter();
        final String output = docWriterForTest.formatOutput(displayName, testMethod)
                .replaceAll("(^)\\[#", "//[#")
                .replaceAll("\\n\\[#", "\n//[#");

        doc.write(String.format("Calling formatOutput with DisplayName=\"%s\" and Method=%s provides",
                displayName, testMethod.getName()), "");

        doc.write("****", output, "****", "");
    }

    public String includeSourceWithTag(String tag) {
        final int nb_folder = this.getClass().getPackage().getName().split("\\.").length + 1;
        final String rootPath = IntStream.range(0, nb_folder)
                .mapToObj(i -> "..")
                .collect(Collectors.joining("/"));
        
        return String.join("\n",
                "[source, java, indent=0]",
                "----",
                String.format("include::"+rootPath+"/java/%s.java[tag=%s]",
                        getClass().getName().replace(".", "/"),
                        tag),
                "----");
    }

    public void simple_method_to_format_title() {

    }

    @Test
    @AsciidocFormatterTest.TestOption(includeMethodDoc = "formatTitle")
    @DisplayName("Formatted title")
    public void title(TestInfo testinfo) throws NoSuchMethodException {
        JavaProjectBuilder builder = new JavaProjectBuilder();
        final JavaClass stringClass = builder.getClassByName(String.class.getCanonicalName());
        final JavaClass methodClass = builder.getClassByName(Method.class.getCanonicalName());

        final Optional<AsciidocFormatterTest.TestOption> annotation = Optional.ofNullable(testinfo.getTestMethod()
                .get()
                .getAnnotation(AsciidocFormatterTest.TestOption.class));
        annotation.map(AsciidocFormatterTest.TestOption::includeMethodDoc)
                .filter(name -> !name.isEmpty())
                .map(methodName -> CodeExtractor.getComment(DocWriter.class, methodName, Arrays.asList(stringClass, methodClass)))
                .ifPresent(doc -> write(doc.get() + "\n"));


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

    @NotIncludeToDoc
// tag::MyTestWithComment[]
    private static class MyTestWithComment {
        private static final DocWriter docWriter = new DocWriter();
        @RegisterExtension
        static ApprovalsExtension extension = new ApprovalsExtension(docWriter);

        /**
         * To decribe a method, you can add a comment.
         * It will be added under title.
         */
        @Test
        public void testA() {
            docWriter.write("In my *test*");
        }

    }
// end::MyTestWithComment[]
}


