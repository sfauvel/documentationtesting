package org.sfvl.docformatter;

import org.sfvl.doctesting.*;

import java.io.IOException;

public class FormatterDocumentation extends MainDocumentation {


    public static void main(String... args) throws IOException {
        final FormatterDocumentation generator = new FormatterDocumentation();

        final Class<AsciidocFormatterTest> classToGenerate = AsciidocFormatterTest.class;

        generator.generate(classToGenerate);
    }

}
