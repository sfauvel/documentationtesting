package org.sfvl.test_tools;

import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.writer.DocWriter;

public class FastApprovalsExtension extends ApprovalsExtension<DocWriter<Formatter>, Formatter> {

    public FastApprovalsExtension() {
        super(new FastDocWriter(Config.FORMATTER));
    }
}
