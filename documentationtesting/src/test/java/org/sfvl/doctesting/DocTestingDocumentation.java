package org.sfvl.doctesting;

import java.io.IOException;

public class DocTestingDocumentation extends MainDocumentation {

    @Override
    protected String getHeader() {
        return ":source-highlighter: rouge\n" +
                getDocumentOptions() +
                "= " + DOCUMENTATION_TITLE + "\n\n" +
                generalInformation();
    }

    public static void main(String... args) throws IOException {
        final DocTestingDocumentation generator = new DocTestingDocumentation();

        generator.generate();
    }

}
