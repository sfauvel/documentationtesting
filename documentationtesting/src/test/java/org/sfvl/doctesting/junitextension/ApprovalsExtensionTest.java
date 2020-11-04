package org.sfvl.doctesting.junitextension;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.sfvl.doctesting.NotIncludeToDoc;

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
                "","");

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
    public void using_displayName() {
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