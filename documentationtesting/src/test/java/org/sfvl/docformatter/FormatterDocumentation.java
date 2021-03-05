package org.sfvl.docformatter;

import org.sfvl.doctesting.*;

import java.io.IOException;

public class FormatterDocumentation extends MainDocumentation {

    public void generate() throws IOException {
        generate(AsciidocFormatterTest.class);
    }

    public static void main(String... args) throws IOException {
        new FormatterDocumentation().generate();
    }

}
