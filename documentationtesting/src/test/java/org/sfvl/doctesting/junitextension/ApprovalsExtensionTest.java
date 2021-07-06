package org.sfvl.doctesting.junitextension;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.utils.*;
import org.sfvl.samples.FailingTest;
import org.sfvl.samples.MyCustomWriterTest;
import org.sfvl.samples.MyTest;
import org.sfvl.test_tools.OnlyRunProgrammatically;
import org.sfvl.test_tools.TestRunnerFromTest;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@DisplayName("Approvals extension")
@ClassToDocument(clazz = ApprovalsExtension.class)
public class ApprovalsExtensionTest {

    private AsciidocFormatter formatter = new AsciidocFormatter();
    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    @Test
    @DisplayName(value = "Creating a test using ApprovalsExtension")
    public void using_extension() {
        final Class<?> testClass = MyTest.class;

        runTestAndWriteResultAsComment(testClass);

        doc.write("This is an example to create a simple test using `" + ApprovalsExtension.class.getSimpleName() + "`.", "", "You have to write a class and add `" + RegisterExtension.class.getSimpleName() + "` annotation on an attribute.", "This extension will check that content of `" + DocWriter.class.getSimpleName() + "` has not changed since the last time.", "`" + DocWriter.class.getSimpleName() + "` passed to the `" + ApprovalsExtension.class.getSimpleName() + "` is used to indicated what we want to write to the output.", "", "");

        doc.write(".Test example using `" + ApprovalsExtension.class.getSimpleName() + "`", extractSourceWithTag(testClass.getSimpleName(), testClass), "", "");

        final Method method = FindLambdaMethod.getMethod(MyTest::test_A);
        final Path approvedPath = new DocPath(method).approved().from(this.getClass());
        doc.write("When executing test method `" + method.getName() + "`, a file `" + approvedPath.getFileName() + "` is generated and contains the following text",
                "----", formatter.include(approvedPath.toString()), "----", "Filename and title come from method name.", "The chapter content contains what was written using `" + DocWriter.class.getSimpleName() + "`");
    }

    @Test
    public void using_displayName() throws IOException {
        final Class<?> testClass = UsingDisplayNameTest.class;

        runTestAndWriteResultAsComment(testClass);

        doc.write("You can use DisplayName annotation to customize test title");

        doc.write(".Test example using DisplayName", extractSourceWithTag(testClass.getSimpleName(), this.getClass(), testClass), "", "");

        final String testMethod = FindLambdaMethod.getName(UsingDisplayNameTest::test_A);
        final String filename = "_" + testClass.getSimpleName() + "." + testMethod + ".approved.adoc";
        doc.write("Generated file with DisplayName content as title", "----", formatter.include(filename), "----");
    }

    @Test
    public void using_a_custom_writer() {
        final Class<?> testClass = MyCustomWriterTest.class;

        runTestAndWriteResultAsComment(testClass);

        doc.write("It's possible to give a specific DocWriter to `" + ApprovalsExtension.class.getSimpleName() + "`",
                "and modify what it will be written in the final document.",
                "", "");

        doc.write(".Test example using `" + ApprovalsExtension.class.getSimpleName() + "`", extractSourceWithTag(testClass.getSimpleName(), testClass), "", "");

        final Method method = FindLambdaMethod.getMethod(MyCustomWriterTest::test_A);
        final Path approvedPath = new DocPath(method).approved().from(this.getClass());
        doc.write("When executing test method `" + method.getName() + "`, a file `" + approvedPath.getFileName() + "` is generated and contains the following text",
                "----",
                formatter.include(approvedPath.toString()),
                "----");
    }

    @Test
    public void nested_class() throws IOException {
        doc.write("Nested class can be used to organize tests.", "Each nested class create a nested title.", "");

        final Class<?> testClass = DemoNestedTest.class;
        runTestAndWriteResultAsComment(testClass);

        doc.write("", "", ".Test example using nested class", extractSourceWithTag(testClass.getSimpleName(), this.getClass(), testClass), "", "");

        final Path generatedFilePath = Paths.get("", getClass().getPackage().getName().split("\\."));
        doc.write("Generated files in `" + generatedFilePath + "`:", "", Files.list(doc.getDocPath().resolve(generatedFilePath))
                .map(file -> file.getFileName().toString())
                .filter(filename -> filename.startsWith("_" + DemoNestedTest.class.getSimpleName() + "."))
                .filter((filename -> filename.endsWith(".approved.adoc")))
                .sorted()
                .map(filename -> "* " + filename)
                .collect(Collectors.joining("\n\n")));

        final OnePath approved = new DocPath(testClass).approved();
        doc.write("", "", ".Document generated", "----", Files.lines(approved.path())
                .collect(Collectors.joining("\n"))
                .replaceAll("\\ninclude::", "\n\\\\include::"), "----");

        String style = "++++\n" +
                "<style>\n" +
                ".adocRendering {\n" +
                "    padding: 1em;\n" +
                "    background: #fffef7;\n" +
                "    border-color: #e0e0dc;\n" +
                "    -webkit-box-shadow: 0 1px 4px #e0e0dc;\n" +
                "    box-shadow: 0 1px 4px #e0e0dc;\n" +
                "}\n" +
                "</style>\n" +
                "++++";
        doc.write("", "", style, "", "_final rendering_", "[.adocRendering]", formatter.include("_" + testClass.getSimpleName() + ".approved.adoc", 1));

    }

    @Test
    public void document_with_all_tests_in_a_testclass() throws IOException {
        doc.write("At the end of a test, a file is created including files generated on each test.", "", "`" + ApprovalsExtension.class.getSimpleName() + "` must be static to be able to run `" + AfterAll.class.getSimpleName() + "` callback.");

        final Class<?> testClass = MyTest.class;
        runTestAndWriteResultAsComment(testClass);

        doc.write("", "", ".Test example used to generate class document", extractSourceWithTag(testClass.getSimpleName(), testClass), "", "");

        doc.write("Class `" + testClass.getSimpleName() + "` is in package `" + testClass.getPackage().getName() + "`.");
        final OnePath approved = new DocPath(MyTest.class).approved();

        doc.write("", "", adocFileSourceEscaped(approved));

        String style = "++++\n" +
                "<style>\n" +
                ".adocRendering {\n" +
                "    padding: 1em;\n" +
                "    background: #fffef7;\n" +
                "    border-color: #e0e0dc;\n" +
                "    -webkit-box-shadow: 0 1px 4px #e0e0dc;\n" +
                "    box-shadow: 0 1px 4px #e0e0dc;\n" +
                "}\n" +
                "</style>\n" +
                "++++";
        doc.write("", "", style, "", "_final rendering_", "[.adocRendering]", formatter.include(approved.from(this.getClass()).toString(), 1));

    }

    /**
     * When a test fails, the error is written in the final document.
     * It's help to understand and investigate on the problem.
     */
    @Test
    public void failing_test_output() throws IOException {
        doc.write("When the test fails, the reason (exception) is written into the generated document.", "");

        final Class<?> testClass = FailingTest.class;
        runTestAndWriteResultAsComment(testClass);

        doc.write("", "", ".Test example used to generate class document", extractSourceWithTag(testClass.getSimpleName(), testClass), "", "");

        final Method method = FindLambdaMethod.getMethod(FailingTest::failing_test);
        final DocPath docPath = new DocPath(method);
        final Path documentationPath = docPath.received().path();

        AtomicInteger stacktraceLineCount = new AtomicInteger(0);
        Predicate<String> isStackLine = line -> line.startsWith("	at ");
        // We truncate stack trace to avoid to have an ouput that change from on execution from another.
        doc.write("", "", ".Document generated (exception stack trace is truncated)", "------",
                escapedAdocSpecialKeywords(Files.lines(documentationPath)
                // We truncate stack trace to avoid to have an ouput that change from on execution from another.
                .filter(line -> !isStackLine.test(line) || stacktraceLineCount.incrementAndGet() < 3)
                .collect(Collectors.joining("\n"))),
                "------");

        String style = "++++\n" +
                "<style>\n" +
                ".adocRendering {\n" +
                "    padding: 1em;\n" +
                "    background: #fffef7;\n" +
                "    border-color: #e0e0dc;\n" +
                "    -webkit-box-shadow: 0 1px 4px #e0e0dc;\n" +
                "    box-shadow: 0 1px 4px #e0e0dc;\n" +
                "}\n" +
                "</style>\n" +
                "++++";

        doc.write("",
                "",
                style,
                "",
                ":leveloffset: +1",
                "_final rendering_",
                "[.adocRendering]",
                formatter.include_with_lines(docPath.received().from(this.getClass()).toString(), 1, 14),
                "...",
                "----",
                "// We add the line below to close truncated block open in included file",
                ":leveloffset: -1");
    }

    private void runTestAndWriteResultAsComment(Class<?> testClass) {
        final TestRunnerFromTest.Results results = new TestRunnerFromTest().runTestClass(testClass);
        String[] texts = new String[]{"", String.format("// Test result for %s: %s", testClass.getSimpleName(), results.sucess() ? "Success" : "Fails"), ""};
        doc.write(texts);
    }

    private String extractSourceWithTag(String tag, Class<?> classToIdentifySourceClass, Class<?> testClass) {
        final String source = String.join("\n",
                "[source, java, indent=0]",
                "----",
                CodeExtractor.extractCodeBetween(CodeExtractor.classSource(classToIdentifySourceClass, testClass), "tag::" + tag + "[]", "end::" + tag + "[]").trim(),
                "----");
        return source;
    }

    private String extractSourceWithTag(String tag, Class<?> testClass) {
        return extractSourceWithTag(tag, testClass, testClass);
    }

    private String adocFileSourceEscaped(OnePath approved) throws IOException {
        final String collect = Files.lines(approved.path())
                .collect(Collectors.joining("\n"));
        return String.join("\n",
                ".Document generated",
                "----",
                escapedAdocSpecialKeywords(collect),
                "----");
    }

    private String escapedAdocSpecialKeywords(String collect) {
        return collect
                .replaceAll("(^|\\n)include::", "$1\\\\include::")
                .replaceAll("(^|\\n)ifndef::", "$1\\\\ifndef::")
                .replaceAll("(^|\\n)ifdef::", "$1\\\\ifdef::")
                .replaceAll("(^|\\n)endif::", "$1\\\\endif::");
    }
}

@NotIncludeToDoc
@OnlyRunProgrammatically
// tag::UsingDisplayNameTest[]
@DisplayName("Title for the document")
class UsingDisplayNameTest {
    @RegisterExtension
    static final ApprovalsExtension doc = new SimpleApprovalsExtension();

    @Test
    @DisplayName("Title for this test")
    public void test_A() {
        doc.write("In my *test*");
    }
}
// end::UsingDisplayNameTest[]

@NotIncludeToDoc
@OnlyRunProgrammatically
// tag::DemoNestedTest[]
/**
 * Demo of a simple usage to generate documentation.
 */
class DemoNestedTest {
    @RegisterExtension
    static final ApprovalsExtension writer = new SimpleApprovalsExtension();

    /**
     * Document of Addition operations.
     */
    @Nested
    class Adding {

        @Test
        @DisplayName("Adding 2 simple numbers")
        public void should_be_5_when_adding_2_and_3() {
            writer.write(String.format("%d + %d = %d", 2, 3, 2 + 3));
        }

        /**
         * A nested test.
         */
        @Test
        @DisplayName("Adding 3 simple numbers")
        public void should_be_9_when_adding_2_3_and_4() {
            writer.write(String.format("%d + %d + %d = %d", 2, 3, 4, 2 + 3 + 4));
        }
    }

    @Nested
    class Multiply {
        @Test
        @DisplayName("Multiply 2 simple numbers")
        public void should_be_12_when_multiply_4_and_3() {
            writer.write(String.format("%d * %d = %d", 4, 3, 4 * 3));
        }
    }
}
// end::DemoNestedTest[]

