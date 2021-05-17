package org.sfvl.doctesting.junitinheritance;

import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.DocumentationNamer;

/**
 * Base class for test.
 *
 * It checks that everything written during test is identical to the approved content.
 */
public class SelectableBase extends DocAsTestBase {
    @Override
    protected void approvalAfterTestSpecific(String content, DocPath docPath) throws Exception {
        DocAsTestBase testBase = "approvals".equals(System.getProperty("approved_with"))
                ? new ApprovalsBase()
                : new GitBase();
        System.out.println("Approved with " + testBase.getClass().getSimpleName());
        testBase.approvalAfterTestSpecific(content, docPath);
    }
}
