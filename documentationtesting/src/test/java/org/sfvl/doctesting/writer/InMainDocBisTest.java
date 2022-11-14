package org.sfvl.doctesting.writer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.test_tools.FastApprovalsExtension;

@NotIncludeToDoc
class InMainDocBisTest {
    @RegisterExtension
    static ApprovalsExtension doc = new FastApprovalsExtension();

    @Test
    @DisplayName("Title for this test")
    public void testX() {
        doc.write("In my *test*");
    }
}
