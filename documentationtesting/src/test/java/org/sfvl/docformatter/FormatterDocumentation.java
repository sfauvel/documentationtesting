package org.sfvl.docformatter;

import org.sfvl.doctesting.ClassDocumentation;
import org.sfvl.doctesting.Document;
import org.sfvl.doctesting.DocumentationBuilder;
import org.sfvl.doctesting.DocumentationNamer;

import java.io.IOException;

public class FormatterDocumentation extends DocumentationBuilder {

    public FormatterDocumentation() {
        super("Documentation");
        withClassesToInclude(AsciidocFormatterTest.class);
        withLocation(FormatterDocumentation.class.getPackage());
        withStructureBuilder(FormatterDocumentation.class,
                b -> b.getDocumentOptions(),
                b -> "= " + getDocumentTitle(),
                b -> b.includeClasses()
        );
    }

    public static void main(String... args) throws IOException {
        Document.produce(new FormatterDocumentation());
    }

}
