package org.sfvl.doctesting.junitinheritance;

import org.approvaltests.Approvals;
import org.approvaltests.namer.ApprovalNamer;
import org.approvaltests.writers.ApprovalTextWriter;
import org.sfvl.doctesting.DocumentationNamer;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Base class for test.
 *
 * It checks that everything written during test is identical to the approved content.
 */
public class ApprovalsBase extends DocAsTestBase {

    @Override
    protected void approvalAfterTestSpecific(final String content, final DocumentationNamer documentationNamer) throws Exception {
        ApprovalNamer approvalNamer = new ApprovalNamer() {

            @Override
            public String getApprovalName() {
                return documentationNamer.getApprovalName();
            }

            @Override
            public String getSourceFilePath() {
                return documentationNamer.getSourceFilePath();
            }
        };

        Approvals.verify(
                new ApprovalTextWriter(content, "adoc"),
                approvalNamer,
                Approvals.getReporter());
    }

}
