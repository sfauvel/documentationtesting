package org.sfvl.doctesting.junitextension;

import org.approvaltests.namer.ApprovalNamer;
import org.sfvl.doctesting.utils.DocPath;

import java.io.File;

public class DocAsTestApprovalNamer implements ApprovalNamer {
    private final DocPath docPath;

    public DocAsTestApprovalNamer(DocPath docPath) {
        this.docPath = docPath;
    }

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
}
