package org.sfvl.samples;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.test_tools.OnlyRunProgrammatically;

import static org.junit.jupiter.api.Assertions.fail;

@NotIncludeToDoc
@OnlyRunProgrammatically
public
// tag::FailingTest[]
class FailingTest {
    @RegisterExtension
    static final ApprovalsExtension doc = new SimpleApprovalsExtension();

    @Test
    public void failing_test() {
        doc.write("Some information before failure.", "", "");
        fail("Problem on the test, it fails.");
        doc.write("Information added after failure are not in the final document.", "");
    }
}
