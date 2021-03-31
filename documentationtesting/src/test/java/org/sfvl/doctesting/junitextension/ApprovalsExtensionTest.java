package org.sfvl.doctesting.junitextension;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.sfvl.doctesting.DocWriter;
import org.sfvl.doctesting.DocumentationNamer;
import org.sfvl.doctesting.NotIncludeToDoc;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@DisplayName("Approvals extension")
@ClassToDocument(clazz = ApprovalsExtension.class)
class ApprovalsExtensionTest {

    private static final DocWriter docWriter = new DocWriter();
    @RegisterExtension
    static ApprovalsExtension extension = new ApprovalsExtension(docWriter);

    private void write(String... texts) {
        docWriter.write(texts);
    }

    @Test
    public void using_extension() {
        final Class<?> testClass = MyTest.class;

        runTestClass(testClass);

        write("This is an example of `" + ApprovalsExtension.class.getSimpleName() + "` usage.",
                "",
                "You have to write a class and add `" + RegisterExtension.class.getSimpleName() + "` annotation on an attribute.",
                "This extension will check that content of `" + DocWriter.class.getSimpleName() + "` has not changed since the last time.",
                "`" + DocWriter.class.getSimpleName() + "` passed to the `" + ApprovalsExtension.class.getSimpleName() + "` is used to indicated what we want to write to the output.",
                "", "");

        write(".Test example using `" + ApprovalsExtension.class.getSimpleName() + "`",
                includeSourceWithTag(testClass.getSimpleName()),
                "", "");

        final String testMethod = FindLambdaMethod.getName(MyTest::test_A);
        final String filename = testClass.getSimpleName() + "." + testMethod + ".approved.adoc";
        write("When executing test method `" + testMethod + "`, a file `" + filename + "` is generated and contains the following text",
                "----",
                "include::" + filename + "[]",
                "----",
                "Filename and title come from method name.",
                "The chapter content contains what was written using `" + DocWriter.class.getSimpleName() + "`");

    }

    @Test
    public void using_displayName() throws IOException {
        final Class<?> testClass = UsingDisplayNameTest.class;

        runTestClass(testClass);

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
        runTestClass(testClass);

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

        final Path documentPath = DocumentationNamer.toAsciiDocFilePath(testClass);
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
                "include::" + testClass.getSimpleName() + ".adoc[leveloffset=+1]"
        );

    }

    @Test
    public void document_with_all_tests_in_a_testclass() throws IOException {
        write("At the end of a test, a file is created including file generated on each test.",
                "",
                "`" + ApprovalsExtension.class.getSimpleName() + "` must be static to be able to run `" + AfterAll.class.getSimpleName() + "` callback.");

        final Class<?> testClass = MyTest.class;
        runTestClass(testClass);

        write("", "", ".Test example used to generate class document",
                includeSourceWithTag(testClass.getSimpleName()),
                "", "");

        final Path generatedFilePath = Paths.get("", getClass().getPackage().getName().split("\\."));
        final Path documentationPath = extension.getDocPath().resolve(generatedFilePath).resolve(testClass.getSimpleName() + ".adoc");

        write("", "", ".Document generated",
                "----",
                Files.lines(documentationPath)
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
                "include::" + testClass.getSimpleName() + ".adoc[leveloffset=+1]"
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
        runTestClass(testClass);

        write("", "", ".Test example used to generate class document",
                includeSourceWithTag(testClass.getSimpleName()),
                "", "");

        final Path generatedFilePath = Paths.get("", getClass().getPackage().getName().split("\\."));
        final Path documentationPath = extension.getDocPath().resolve(generatedFilePath).resolve(testClass.getSimpleName() + ".adoc");

        write("", "", ".Document generated",
                "----",
                Files.lines(documentationPath)
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
                "include::" + testClass.getSimpleName() + ".adoc[leveloffset=+1]"
        );

    }


    public void runTestClass(Class<?> testClass) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(testClass))
                .build();
        Launcher launcher = LauncherFactory.create();
//        TestPlan testPlan = launcher.discover(request);
//        launcher.registerTestExecutionListeners(listener);
        OnlyRunProgrammaticallyCondition.enable();
        launcher.execute(request);
        OnlyRunProgrammaticallyCondition.disable();
    }

    public String includeSourceWithTag(String tag) {
        return String.join("\n",
                "[source, java, indent=0]",
                "----",
                String.format("include::../../../../../java/%s.java[tag=%s]",
                        getClass().getName().replace(".", "/"),
                        tag),
                "----");
    }


}

class OnlyRunProgrammaticallyCondition implements ExecutionCondition {

    static public boolean RUN_TEST_PROGRAMATICALLY = false;

    public static boolean isEnabled() {
        return RUN_TEST_PROGRAMATICALLY;
    }

    public static void enable() {
        RUN_TEST_PROGRAMATICALLY = true;
    }

    public static void disable() {
        RUN_TEST_PROGRAMATICALLY = false;
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        if (isEnabled()) {
            return ConditionEvaluationResult.enabled("Test run programmatically");
        } else {
            return ConditionEvaluationResult.disabled("Test could only be launched programmatically.");
        }
    }
}

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(OnlyRunProgrammaticallyCondition.class)
@interface OnlyRunProgrammatically {
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
// tag::MyTest[]
class MyTest {
    private static final DocWriter docWriter = new DocWriter();
    @RegisterExtension
    static final ApprovalsExtension extension = new ApprovalsExtension(docWriter);

    @Test
    public void test_A() {
        docWriter.write("In my *test*");
    }

}
// end::MyTest[]

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

