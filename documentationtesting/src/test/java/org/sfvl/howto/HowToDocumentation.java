package org.sfvl.howto;

import org.sfvl.doctesting.MainDocumentation;

import java.io.IOException;

public class HowToDocumentation extends MainDocumentation {

    @Override
    protected String getHeader() {
        return joinParagraph(
                ":source-highlighter: rouge\n" + getDocumentOptions(),
                "= " + DOCUMENTATION_TITLE,
                generalInformation());
    }

    public static void main(String... args) throws IOException {
        final HowToDocumentation generator = new HowToDocumentation();

        generator.generate();
    }

}
