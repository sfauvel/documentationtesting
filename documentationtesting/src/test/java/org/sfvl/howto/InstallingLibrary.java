package org.sfvl.howto;

import org.sfvl.doctesting.MainDocumentation;

import java.nio.file.Path;

public class InstallingLibrary  extends MainDocumentation {

    public InstallingLibrary() {
        super("Installing Documentation testing");
    }

    @Override
    public String getHeader() {

        final String header = formatter.paragraphSuite(
                getDocumentOptions(),
                "= " + documentationTitle,
                generalInformation());
        return header;
    }
    @Override
    protected String getMethodDocumentation(String packageToScan, Path docFilePath) {
        return "To be written";

    }
}