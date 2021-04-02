package org.sfvl.doctesting.writer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.utils.DocWriter;

@NotIncludeToDoc
class InMainDocBisTest {
    private static final DocWriter docWriter = new DocWriter();
    @RegisterExtension
    static ApprovalsExtension extension = new ApprovalsExtension(docWriter);

    @Test
    @DisplayName("Title for this test")
    public void testX() {
        docWriter.write("In my *test*");
    }
}
