package org.sfvl.samples;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.test_tools.OnlyRunProgrammatically;

@NotIncludeToDoc
@OnlyRunProgrammatically
public class MyTestWithTests {
    @RegisterExtension
    static final ApprovalsExtension doc = new SimpleApprovalsExtension();

    @Test
    public void test_A() {
    }
    @Test
    public void test_B() {
    }
    @Test
    public void test_C() {
    }

}
