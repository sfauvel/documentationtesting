package org.sfvl.howto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.NoTitle;
import org.sfvl.doctesting.utils.OnePath;

import java.nio.file.Path;
import java.nio.file.Paths;

public class KnownIssues {

    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    private final AsciidocFormatter formatter = new AsciidocFormatter();

    @Test
    @NoTitle
    public void knownIssues() {
        final OnePath docPath = new DocPath(Paths.get(""), "knownIssues").resource();
        final Path from = docPath.from(new DocPath(this.getClass()).approved());

        doc.write(formatter.include(DocPath.toAsciiDoc(from), 1));
    }

}
