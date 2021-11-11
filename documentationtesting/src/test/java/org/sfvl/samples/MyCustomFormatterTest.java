package org.sfvl.samples;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.writer.DocWriter;

@NotIncludeToDoc
@org.sfvl.test_tools.OnlyRunProgrammatically
public
// >>>MyCustomFormatterTest
class MyCustomFormatterTest {

    @RegisterExtension
    static final ApprovalsExtension doc = new ApprovalsExtension(
            new DocWriter(
                    new AsciidocFormatter() {
                        @Override
                        /// Add the word `Warning` before the message.
                        public String warning(String message) {
                            return super.warning("Warning: " + message);
                        }
                    })
    );

    @Test
    public void test_A() {
        doc.write(doc.getFormatter().warning("My custom warning."));
    }

}
// <<<MyCustomFormatterTest
