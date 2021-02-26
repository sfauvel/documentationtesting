package org.sfvl.docformatter;

import org.sfvl.doctesting.MainDocumentation;

import java.io.IOException;

public class FormatterDocumentation extends MainDocumentation {


    @Override
    protected String getHeader() {
        return formatter.paragraphSuite(
                ":source-highlighter: rouge\n" + getDocumentOptions(),
                "= " + DOCUMENTATION_TITLE,
                generalInformation());
    }

    public static void main(String... args) throws IOException {
        final FormatterDocumentation generator = new FormatterDocumentation();

        generator.generate();
    }

}
