package org.sfvl.doctesting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;

/**
 * Class comment is added to description.
 */
@DisplayName("Title for the document")
@NotIncludeToDoc
class InMainDocTest {
    private static final DocWriter docWriter = new DocWriter();
    @RegisterExtension
    static ApprovalsExtension extension = new ApprovalsExtension(docWriter);

    @Test
    @DisplayName("Title for this test")
    public void testA() {
        docWriter.write("In my *test*");
    }

    @Test
    @DisplayName("Title for this test")
    public void testB() {
        docWriter.write("In my *test*");
    }
}
