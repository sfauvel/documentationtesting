package org.sfvl.docformatter;

import org.sfvl.doctesting.MainDocumentation;

import java.io.IOException;

public class FormatterDocumentation extends MainDocumentation {

    public static void main(String... args) throws IOException {
        final FormatterDocumentation generator = new FormatterDocumentation();

        generator.generate("org.sfvl.docformatter");
    }

}
