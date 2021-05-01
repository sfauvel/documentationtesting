package org.sfvl.doctesting.junitextension;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.test_tools.OnlyRunProgrammatically;
import org.sfvl.doctesting.test_tools.TestRunnerFromTest;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocWriter;
import org.sfvl.doctesting.utils.DocumentationNamer;
import org.sfvl.samples.MyTest;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("Approvals extension")
@ClassToDocument(clazz = ApprovalsExtension.class)
public class ApprovalsExtensionTest {

    private static final DocWriter docWriter = new DocWriter();
    @RegisterExtension
    static ApprovalsExtension extension = new ApprovalsExtension(docWriter);

    private void write(String... texts) {
        docWriter.write(texts);
    }

    @Test
    @DisplayName(value = "Creating a test using ApprovalsExtension")
    public void using_extension() {
        final Class<?> testClass = MyTest.class;

        new TestRunnerFromTest().runTestClass(testClass);

        write("This is an example to create a simple test using `" + ApprovalsExtension.class.getSimpleName() + "`.",
                "",
                "You have to write a class and add `" + RegisterExtension.class.getSimpleName() + "` annotation on an attribute.",
                "This extension will check that content of `" + DocWriter.class.getSimpleName() + "` has not changed since the last time.",
                "`" + DocWriter.class.getSimpleName() + "` passed to the `" + ApprovalsExtension.class.getSimpleName() + "` is used to indicated what we want to write to the output.",
                "", "");

        write(".Test example using `" + ApprovalsExtension.class.getSimpleName() + "`",
                includeSourceWithTag(testClass.getSimpleName(), testClass),
                "", "");

        final Method method = FindLambdaMethod.getMethod(MyTest::test_A);
        final Path approvedPath = getRelativizedApprovedPath(method, Config.DOC_PATH);
        write("When executing test method `" + method.getName() + "`, a file `" + approvedPath.getFileName() + "` is generated and contains the following text",
                "----",
                "include::" + approvedPath + "[]",
                "----",
                "Filename and title come from method name.",
                "The chapter content contains what was written using `" + DocWriter.class.getSimpleName() + "`");

    }

    @Test
    public void using_displayName() throws IOException {
        final Class<?> testClass = UsingDisplayNameTest.class;

        new TestRunnerFromTest().runTestClass(testClass);

        write("You can use DisplayName annotation to customize test title");

        write(".Test example using DisplayName",
                includeSourceWithTag(testClass.getSimpleName()),
                "", "");

        final String testMethod = FindLambdaMethod.getName(UsingDisplayNameTest::test_A);
        final String filename = testClass.getSimpleName() + "." + testMethod + ".approved.adoc";
        write("Generated file with DisplayName content as title",
                "----",
                "include::" + filename + "[]",
                "----");
    }

    @Test
    public void nested_class() throws IOException {
        write("Nested class can be used to organize tests.",
                "Each nested class create a nested title.",
                "",
                "`" + ApprovalsExtension.class.getSimpleName() + "` must be register on each test " +
                        "when `̀" + DocWriter.class.getSimpleName() + "` could be declare once on enclosing class.");

        final Class<?> testClass = DemoNestedTest.class;
        new TestRunnerFromTest().runTestClass(testClass);

        write("", "", ".Test example using nested class",
                includeSourceWithTag(testClass.getSimpleName()),
                "", "");

        final Path generatedFilePath = Paths.get("", getClass().getPackage().getName().split("\\."));
        write("Generated files in `" + generatedFilePath + "`:", "",
                Files.list(extension.getDocPath().resolve(generatedFilePath))
                        .map(file -> file.getFileName().toString())
                        .filter(filename -> filename.startsWith(DemoNestedTest.class.getSimpleName() + "."))
                        .filter((filename -> filename.endsWith(".approved.adoc")))
                        .sorted()
                        .map(filename -> "* " + filename)
                        .collect(Collectors.joining("\n\n")));

        final Path documentPath = new DocumentationNamer(extension.getDocPath(), testClass).getFilePath();
        write("", "", ".Document generated",
                "----",
                Files.lines(extension.getDocPath().resolve(documentPath))
                        .collect(Collectors.joining("\n"))
                        .replaceAll("\\ninclude::", "\n\\\\include::"),
                "----");

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
        write("", "", style, "", "_final rendering_", "[.adocRendering]",
                "include::" + testClass.getSimpleName() + ".approved.adoc[leveloffset=+1]"
        );

    }

    @Test
    public void document_with_all_tests_in_a_testclass() throws IOException {
        write("At the end of a test, a file is created including file generated on each test.",
                "",
                "`" + ApprovalsExtension.class.getSimpleName() + "` must be static to be able to run `" + AfterAll.class.getSimpleName() + "` callback.");

        final Class<?> testClass = MyTest.class;
        new TestRunnerFromTest().runTestClass(testClass);

        write("", "", ".Test example used to generate class document",
                includeSourceWithTag(testClass.getSimpleName(),testClass),
                "", "");

        final DocumentationNamer documentationNamer = new DocumentationNamer(Config.DOC_PATH, MyTest.class);

        write("", "", ".Document generated",
                "----",
                Files.lines(documentationNamer.getApprovedPath(Paths.get("")))
                        .collect(Collectors.joining("\n"))
                        .replaceAll("\\ninclude::", "\n\\\\include::"),
                "----");

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
        write("", "", style, "", "_final rendering_", "[.adocRendering]",
                "include::" + getRelativizedApprovedPath(documentationNamer, Config.DOC_PATH) + "[leveloffset=+1]"
        );

    }

    /**
     * When a test fails, the error is written in the final document.
     * It's help to understand and investigate on the problem.
     */
    @Test
    public void failing_test_output() throws IOException {
        write("When the test fails, the reason (exception) is written into the generated document.",
                "");

        final Class<?> testClass = FailingTest.class;
        new TestRunnerFromTest().runTestClass(testClass);

        write("", "", ".Test example used to generate class document",
                includeSourceWithTag(testClass.getSimpleName()),
                "", "");

        final String fileName = testClass.getSimpleName() + ".failing_test.received.adoc";
        final Path filePath = DocumentationNamer.toPath(testClass.getPackage()).resolve(fileName);
        final Path documentationPath = Config.DOC_PATH.resolve(filePath);

        AtomicInteger stacktraceLineCount = new AtomicInteger(0);
        Predicate<String> isStackLine = line -> line.startsWith("	at ");
        write("", "", ".Document generated (exception stack trace is truncated)",
                "------",
                Files.lines(documentationPath)
                        // We truncate stack trace to avoid to have an ouput that change from on execution from another.
                        .filter(line -> !isStackLine.test(line) || stacktraceLineCount.incrementAndGet() < 3)
                        .collect(Collectors.joining("\n"))
                        .replaceAll("\\ninclude::", "\n\\\\include::"),
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

        write("", "", style, "", "_final rendering_", "[.adocRendering]",
                "include::" + fileName + "[leveloffset=+1]"
        );

    }

    private Path getRelativizedApprovedPath(Method method, Path fromPath) {
        final DocumentationNamer documentationNamer = new DocumentationNamer(Config.DOC_PATH, method);
        return getRelativizedApprovedPath(documentationNamer, fromPath);
    }

    private Path getRelativizedApprovedPath(DocumentationNamer documentationNamer, Path fromPath) {
        return documentationNamer.getApprovedPath(fromPath.resolve(DocumentationNamer.toPath(this.getClass().getPackage())));
    }

    public String includeSourceWithTag(String tag) {
        return includeSourceWithTag(tag, this.getClass());
    }

    private String includeSourceWithTag(String tag, Class<?> aClass) {
        final Path adoc = Config.DOC_PATH.resolve(DocumentationNamer.toPath(this.getClass().getPackage()));
        final Path path = Config.TEST_PATH.resolve(DocumentationNamer.toPath(aClass, "", ".java"));
        final Path relativizedJavaFilePath = adoc.relativize(path);
        return String.join("\n",
                "[source, java, indent=0]",
                "----",
                String.format("include::%s[tag=%s]",
                        relativizedJavaFilePath,
                        tag),
                "----");
    }


}

@NotIncludeToDoc
@OnlyRunProgrammatically
// tag::FailingTest[]
class FailingTest {
    private static final DocWriter docWriter = new DocWriter();
    @RegisterExtension
    static final ApprovalsExtension extension = new ApprovalsExtension(docWriter);

    @Test
    public void failing_test() {
        docWriter.write("Some information before failure.", "", "");
        fail("Problem on the test, it fails.");
        docWriter.write("Information added after failure are not in the final document.", "");
    }
}
// end::FailingTest[]

@NotIncludeToDoc
@OnlyRunProgrammatically
// tag::UsingDisplayNameTest[]
@DisplayName("Title for the document")
class UsingDisplayNameTest {
    private static final DocWriter docWriter = new DocWriter();
    @RegisterExtension
    static final ApprovalsExtension extension = new ApprovalsExtension(docWriter);

    @Test
    @DisplayName("Title for this test")
    public void test_A() {
        docWriter.write("In my *test*");
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
    private static final DocWriter writer = new DocWriter();

    @RegisterExtension
    static final ApprovalsExtension extension = new ApprovalsExtension(writer);

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

