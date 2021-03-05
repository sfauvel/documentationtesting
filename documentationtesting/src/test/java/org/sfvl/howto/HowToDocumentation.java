package org.sfvl.howto;

import org.sfvl.doctesting.MainDocumentation;

import java.io.IOException;

public class HowToDocumentation extends MainDocumentation {
    public HowToDocumentation() {
        super("How to");
    }

    @Override
    protected String getHeader() {
        return formatter.paragraphSuite(
                ":source-highlighter: rouge\n" + getDocumentOptions(),
                "= " + DOCUMENTATION_TITLE,
                generalInformation());
    }

    public static void main(String... args) throws IOException {
        final HowToDocumentation generator = new HowToDocumentation();

        generator.generate();
    }

}
