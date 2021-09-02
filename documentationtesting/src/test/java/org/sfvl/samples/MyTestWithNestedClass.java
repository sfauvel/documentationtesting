package org.sfvl.samples;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;

@NotIncludeToDoc
@org.sfvl.test_tools.OnlyRunProgrammatically
public
// >>>MyTestWithNestedClass
class MyTestWithNestedClass {
    @RegisterExtension
    static final ApprovalsExtension doc = new SimpleApprovalsExtension();

    @Test
    public void test_A() {
        doc.write("In my *test*");
    }

    public class MyNestedClass {

    }
}
// <<<MyTestWithNestedClass
