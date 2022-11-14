// This class may be impacted with an intellij formatter removing some comments between imports.
// In that case, associated tests will not work.
// >>>OneTest
package org.sfvl.samples.justone;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
// <<<OneTest
import org.sfvl.doctesting.NotIncludeToDoc;
// >>>OneTest
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
// <<<OneTest
import org.sfvl.test_tools.OnlyRunProgrammatically;

@NotIncludeToDoc
@OnlyRunProgrammatically

// >>>OneTest
public class OneTest {
    @RegisterExtension
    static final ApprovalsExtension doc = new SimpleApprovalsExtension();

    @Test
    public void test_A() {
        doc.write("In my *test*");
    }

}
// <<<OneTest
