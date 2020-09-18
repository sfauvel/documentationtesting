package org.sfvl.doctesting;

/**
 * Base class for test.
 *
 * It checks that everything written during test is identical to the approved content.
 */
public class SelectableBase extends DocAsTestBase {

    @Override
    protected void approvalAfterTestSpecific(final String content, final DocumentationNamer documentationNamer) throws Exception {
        DocAsTestBase testBase = "approvals".equals(System.getProperty("approved_with"))
                ? new ApprovalsBase()
                : new GitBase();
        System.out.println("Approved with " + testBase.getClass().getSimpleName());
        testBase.approvalAfterTestSpecific(content, documentationNamer);
    }

}
