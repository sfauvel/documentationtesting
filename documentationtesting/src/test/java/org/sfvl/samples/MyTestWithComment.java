package org.sfvl.samples;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.ClassToDocument;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.test_tools.OnlyRunProgrammatically;

/**
 * My comment for MyTestComment
 */
@NotIncludeToDoc
@OnlyRunProgrammatically
public class MyTestWithComment {
    @RegisterExtension
    static final ApprovalsExtension doc = new SimpleApprovalsExtension();

    /**
     * To decribe a method, you can add a comment.
     * It will be added under title.
     */
    @Test
    public void test_A() {
    }

}
