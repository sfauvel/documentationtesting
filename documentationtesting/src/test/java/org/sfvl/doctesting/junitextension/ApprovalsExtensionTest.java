package org.sfvl.doctesting.junitextension;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.sfvl.doctesting.MainDocumentation;
import org.sfvl.doctesting.NotIncludeToDoc;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@DisplayName("ApprovalsExtension")
@ClassToDocument(clazz = ApprovalsExtension.class)
class ApprovalsExtensionTest {

    private final DocWriter docWriter = new DocWriter();
    @RegisterExtension
    ApprovalsExtension extension = new ApprovalsExtension(docWriter);

    private void write(String... texts) {
        docWriter.write(texts);
    }

    @Test
    public void using_extension() {
        final Class<?> testClass = MyTest.class;

        runTestClass(testClass);

        write("This is an example of `" + ApprovalsExtension.class.getSimpleName() + "` usage.",
                "",
                "You have to write a class and add `" + RegisterExtension.class.getSimpleName() + "` annotation on an attribute",
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

        write(".Test example using nested class",
                includeSourceWithTag(testClass.getSimpleName()),
                "", "");

        final Path generatedFilePath = Paths.get("", getClass().getPackage().getName().split("\\."));
        write("Generated files in `" + generatedFilePath + "`:", "",
                Files.list(extension.getDocPath().resolve(generatedFilePath))
                        .map(file -> file.getFileName().toString())
                        .filter(filename -> filename.startsWith(DemoNestedTest.class.getSimpleName() + "."))
                        .filter((filename -> filename.endsWith(".approved.adoc")))
                        .map(filename -> "* " + filename)
                        .collect(Collectors.joining("\n\n")));

        final MainDocumentation mainDocumentation = new MainDocumentation() {
            @Override
            protected String getHeader() {
                return getDocumentOptions() +
                        "= " + DOCUMENTATION_TITLE + "\n\n";
            }

            @Override
            protected Set<Method> getAnnotatedMethod(Class<? extends Annotation> annotation, String packageToScan) {
                return super.getAnnotatedMethod(annotation, packageToScan).stream()
                        .filter(method -> {
                            final Class<?> enclosingClass = method.getDeclaringClass().getEnclosingClass();
                            return DemoNestedTest.class.equals(enclosingClass);
                        })
                        .collect(Collectors.toSet());
            }
        };

        final String documentationFilename = this.getClass().getPackage().getName().replace(".", "/") + "/" + testClass.getSimpleName();
        mainDocumentation.generate(this.getClass().getPackage().getName(), documentationFilename);

        write("", "", ".Document generated",
                "----",
                Files.lines(extension.getDocPath().resolve(Paths.get(documentationFilename + ".adoc")))
                        .collect(Collectors.joining("\n"))
                        .replaceAll("\\ninclude::", "\n\\\\include::"),
                "----");

        write("", "", ".final rendeering",
                "include::" + testClass.getSimpleName() + ".adoc[]"
        );

        write("", "", "This result is obtains from the source code", "");
        write(this.includeSourceWithTag(testClass.getSimpleName()));
    }


    public void runTestClass(Class<?> testClass) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(testClass))
                .build();
        Launcher launcher = LauncherFactory.create();
//        TestPlan testPlan = launcher.discover(request);
//        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);
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

@NotIncludeToDoc
// tag::MyTest[]
class MyTest {
    private final DocWriter docWriter = new DocWriter();
    @RegisterExtension
    ApprovalsExtension extension = new ApprovalsExtension(docWriter);

    @Test
    public void test_A() {
        docWriter.write("In my *test*");
    }

}
// end::MyTest[]

@NotIncludeToDoc
// tag::UsingDisplayNameTest[]
@DisplayName("Title for the document")
class UsingDisplayNameTest {
    private final DocWriter docWriter = new DocWriter();
    @RegisterExtension
    ApprovalsExtension extension = new ApprovalsExtension(docWriter);

    @Test
    @DisplayName("Title for this test")
    public void test_A() {
        docWriter.write("In my *test*");
    }
}
// end::UsingDisplayNameTest[]

// tag::DemoNestedTest[]

/**
 * Demo of a simple usage to generate documentation.
 */
@NotIncludeToDoc
class DemoNestedTest {
    private final DocWriter writer = new DocWriter();

    /**
     * Document of Addition operations.
     */
    @Nested
    class Adding {
        @RegisterExtension
        ApprovalsExtension extension = new ApprovalsExtension(writer);

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
        @RegisterExtension
        ApprovalsExtension extension = new ApprovalsExtension(writer);

        @Test
        @DisplayName("Multiply 2 simple numbers")
        public void should_be_12_when_multiply_4_and_3() {
            writer.write(String.format("%d * %d = %d", 4, 3, 4 * 3));
        }
    }
}
// end::DemoNestedTest[]