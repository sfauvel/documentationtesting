package org.sfvl.howto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.NoTitle;
import org.sfvl.test_tools.FastApprovalsExtension;
import org.sfvl.test_tools.IntermediateHtmlPage;

import java.nio.file.Path;
import java.nio.file.Paths;

@ExtendWith(IntermediateHtmlPage.class)
public class Tutorial {

    @RegisterExtension
    static ApprovalsExtension doc = new FastApprovalsExtension();

    private final AsciidocFormatter formatter = new AsciidocFormatter();

    @Test
    @NoTitle
    public void tutorial() {
        final DocPath docPath = new DocPath(Paths.get(""), "tutorial");
        final Path from = docPath.resource().from(new DocPath(this.getClass()).approved());

        doc.write(
                String.format("include::%s[]", DocPath.toAsciiDoc(new DocPath(InstallingLibrary.class).approved().from(this.getClass()))),
                String.format("include::%s[]", DocPath.toAsciiDoc(from))
        );
    }

}
