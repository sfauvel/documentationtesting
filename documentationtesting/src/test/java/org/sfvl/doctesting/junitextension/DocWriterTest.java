package org.sfvl.doctesting.junitextension;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

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
        final DocWriter docWriterForTest = new DocWriter();
        docWriterForTest.write("Some text added to show DocWriter output.");
        final String output = docWriterForTest.formatOutput("My title", getClass().getMethods()[0]);
        // end::doc_writer_usage[]

        docWriter.write(".DocWriter usage",
                includeSourceWithTag("doc_writer_usage"),
                "", "");

        docWriter.write("",
                "Output provided",
                "****", output, "****");

    }

    @Test
    @DisplayName("Format title")
    public void format_title(TestInfo testInfo) throws NoSuchMethodException {
        final Method testMethod = testInfo.getTestMethod().get();
        final String name = testMethod.getName();

        {
            final DocWriter docWriterForTest = new DocWriter();
            final String output = docWriterForTest.formatOutput("My title", testMethod);

            docWriter.write("When display name is different from test method name, displayName is used as title.",
                    "****", output, "****", "");
        }

        {
            final DocWriter docWriterForTest = new DocWriter();
            final String output = docWriterForTest.formatOutput(testMethod.getName()+"()", testMethod);

            docWriter.write("When display name is the same as test method name, name is reformatted.",
                    "****", output, "****",  "");
        }
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