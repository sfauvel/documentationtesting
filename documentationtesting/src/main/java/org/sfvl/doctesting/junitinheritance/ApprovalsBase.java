package org.sfvl.doctesting.junitinheritance;

import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.namer.ApprovalNamer;
import org.approvaltests.writers.ApprovalTextWriter;
import org.sfvl.doctesting.junitextension.FailureReporter;
import org.sfvl.doctesting.utils.DocPath;

import java.io.File;

/**
 * Base class for test.
 *
 * It checks that everything written during test is identical to the approved content.
 */
public class ApprovalsBase extends DocAsTestBase {

    @Override
    protected void approvalAfterTestSpecific(final String content, final DocPath docPath) throws Exception {
        ApprovalNamer approvalNamer = new ApprovalNamer() {
            @Override
            public String getApprovalName() {
                return "_" + docPath.name();
            }

            @Override
            public String getSourceFilePath() {
                return docPath.approved().folder().toString() + File.separator;
            }

            @Override
            public File getApprovedFile(String extensionWithDot) {
                return new File(this.getSourceFilePath() + "/" + this.getApprovalName() + ".approved" + extensionWithDot);
            }

            @Override
            public File getReceivedFile(String extensionWithDot) {
                return new File(this.getSourceFilePath() + "/" + this.getApprovalName() + ".received" + extensionWithDot);
            }

        };


        final Options options = new Options()
                .forFile().withExtension(".adoc");

        Approvals.verify(
                new ApprovalTextWriter(content, options),
                approvalNamer);

    }

}
