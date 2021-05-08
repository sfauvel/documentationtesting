package org.sfvl.doctesting.writer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;

/**
 * Class comment is added to description.
 */
@DisplayName("Title for the document")
@NotIncludeToDoc
class InMainDocTest {
    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    @Test
    @DisplayName("Title for this test")
    public void testA() {
        doc.write("In my *test*");
    }

    @Test
    @DisplayName("Title for this test")
    public void testB() {
        doc.write("In my *test*");
    }
}
