package org.sfvl.doctesting.junitextension;

import org.sfvl.doctesting.utils.DocWriter;

public class SimpleApprovalsExtension extends ApprovalsExtension<DocWriter> {

    public SimpleApprovalsExtension() {
        super(new DocWriter());
    }
}
