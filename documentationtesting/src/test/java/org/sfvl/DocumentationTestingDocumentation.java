package org.sfvl;

import org.sfvl.docformatter.FormatterDocumentation;
import org.sfvl.doctesting.DocTestingDocumentation;
import org.sfvl.doctesting.MainDocumentation;

import java.io.IOException;
import java.nio.file.Paths;

public class DocumentationTestingDocumentation extends MainDocumentation {

    @Override
    protected String getMethodDocumentation(String packageToScan) {
        return "link:org/sfvl/doctesting/Documentation.html[]\n\n" +
                "link:org/sfvl/docFormatter/Documentation.html[]";
    }

    public static void main(String... args) throws IOException {
        new DocTestingDocumentation().generate();
        new FormatterDocumentation().generate();

        new DocumentationTestingDocumentation().generate("org.sfvl", "Documentation");
    }

}
