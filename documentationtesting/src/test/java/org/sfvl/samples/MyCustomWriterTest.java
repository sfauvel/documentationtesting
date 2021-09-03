package org.sfvl.samples;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.utils.DocWriter;

import java.lang.reflect.Method;

@NotIncludeToDoc
@org.sfvl.test_tools.OnlyRunProgrammatically
public
// >>>MyCustomWriterTest
class MyCustomWriterTest {

    @RegisterExtension
    static final ApprovalsExtension doc = new ApprovalsExtension(
            new DocWriter() {
                @Override
                public void write(String... texts) {
                    super.write(texts);
                    super.write(" // Add a comment after each call to write");
                }

                @Override
                public String formatOutput(String displayName, Method testMethod) {
                    return "// Add an header to the document\n"
                            + super.formatOutput(displayName, testMethod);
                }

                @Override
                public String formatTitle(String displayName, Method method) {
                    return super.formatTitle(displayName, method) + ": Custom title";
                }
            }
    );

    @Test
    public void test_A() {
        doc.write("In my *test*");
    }

}
// <<<MyCustomWriterTest
