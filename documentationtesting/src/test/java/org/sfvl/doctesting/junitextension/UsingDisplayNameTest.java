package org.sfvl.doctesting.junitextension;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.test_tools.OnlyRunProgrammatically;

// >>>UsingDisplayNameTest
@NotIncludeToDoc
@OnlyRunProgrammatically
@DisplayName("Title for the document")
class UsingDisplayNameTest {
    @RegisterExtension
    static final ApprovalsExtension doc = new SimpleApprovalsExtension();

    @Test
    @DisplayName("Title for this test")
    public void test_A() {
        doc.write("In my *test*");
    }
}
// <<<UsingDisplayNameTest