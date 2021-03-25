package org.sfvl.docformatter;

import org.sfvl.doctesting.*;

import java.io.IOException;

public class FormatterDocumentation extends MainDocumentation {

//    public void generate() throws IOException {
////        generate(AsciidocFormatterTest.class);
//        final Path path = DocumentationNamer.toPath(this.getClass().getPackage());
//        generate(this.getClass().getPackage().getName(), path.resolve("TestDoc").toString());
//    }

    protected String getHeader() {

        final String header = formatter.paragraphSuite(
                getDocumentOptions(),
                "= " + documentationTitle,
                generalInformation());
        return header;
    }

    public static void main(String... args) throws IOException {
        final Package packageToDocument = FormatterDocumentation.class.getPackage();
        new FormatterDocumentation().generate(packageToDocument.getName(),
                DocumentationNamer.toPath(packageToDocument).resolve("TestDoc2").toString());
    }

}
