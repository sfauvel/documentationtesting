package org.sfvl.howto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.NoTitle;
import org.sfvl.doctesting.utils.OnePath;
import org.sfvl.test_tools.IntermediateHtmlPage;

import java.nio.file.Path;
import java.nio.file.Paths;

@ExtendWith(IntermediateHtmlPage.class)
public class KnownIssues {

    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    private final AsciidocFormatter formatter = new AsciidocFormatter();

    @Test
    @NoTitle
    public void knownIssues() {
        final Path path = new DocPath("knownIssues").resource().from(this.getClass());

        doc.write(formatter.include(DocPath.toAsciiDoc(path), 0));
    }

}
