package org.sfvl.doctesting.junitextension;

import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.writer.DocWriter;

public class SimpleApprovalsExtension extends ApprovalsExtension<DocWriter<Formatter>, Formatter> {

    public SimpleApprovalsExtension() {
        super(new DocWriter(Config.FORMATTER));
    }
}
