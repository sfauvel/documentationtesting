package org.sfvl.doctesting.junitextension;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.NotIncludeToDoc;

import java.lang.reflect.Method;

@DisplayName("DocWriter")
class DocWriterTest {
    private final DocWriter docWriter = new DocWriter();
    @RegisterExtension
    ApprovalsExtension extension = new ApprovalsExtension(docWriter);

    private void write(String... texts) {
        docWriter.write(texts);
    }

    /**
     * DocWriter is just a buffer.
     * Everything wrote in DocWriter will be returned when asking for output.
     * A title is add to the output.
     *
     * @param testInfo
     * @throws NoSuchMethodException
     */
    @Test
    @DisplayName("DocWriter usage")
    public void doc_writer_usage(TestInfo testInfo) throws NoSuchMethodException {

        // tag::doc_writer_usage[]
        final DocWriter doc = new DocWriter();
        doc.write("Some text added to show DocWriter output.");
        final String output = doc.formatOutput(
                "My title",
                getClass().getMethod("doc_writer_usage", TestInfo.class)
        );
        // end::doc_writer_usage[]

        docWriter.write(".DocWriter usage",
                includeSourceWithTag("doc_writer_usage"),
                //CodeExtractor.extractPartOfMethod()
                "", "");

        docWriter.write("",
                "Output provided",
                "****", output, "****");

    }

    @Test
    @DisplayName("Format title")
    public void format_title() {
        final Method testMethod = FindLambdaMethod.getMethod(DocWriterTest::format_title);
        final String name = testMethod.getName();

        {
            docWriter.write("When display name is different from test method name, displayName is used as title.", "", "");

            writeFormatOutput("My title", testMethod);
        }

        {
            docWriter.write("When display name is the same as test method name, name is reformatted.", "", "");

            writeFormatOutput(testMethod.getName() + "()", testMethod);
        }

        {
            docWriter.write("Test method could have TestInfo parameter.", "", "");

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

        docWriter.write("When test method had a comment, it's written after title.", "", "");

        final DocWriter docWriterForTest = new DocWriter();
        final String output = docWriterForTest.formatOutput(name + "()", testMethod);

        docWriter.write(".Test example with comment on method",
                includeSourceWithTag(MyTestWithComment.class.getSimpleName()),
                "", "");

        docWriter.write("****", output, "****", "");
    }

    public void writeFormatOutput(String displayName, Method testMethod) {
        final DocWriter docWriterForTest = new DocWriter();
        final String output = docWriterForTest.formatOutput(displayName, testMethod);

        docWriter.write(String.format("Calling formatOutput with DisplayName=\"%s\" and Method=%s provides",
                displayName, testMethod.getName()), "");

        docWriter.write("****", output, "****", "");
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
// tag::MyTestWithComment[]
class MyTestWithComment {
    private final DocWriter docWriter = new DocWriter();
    @RegisterExtension
    ApprovalsExtension extension = new ApprovalsExtension(docWriter);

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
