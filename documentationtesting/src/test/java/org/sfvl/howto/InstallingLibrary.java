package org.sfvl.howto;

import org.sfvl.doctesting.writer.Document;
import org.sfvl.doctesting.writer.DocumentationBuilder;

import java.io.IOException;

public class InstallingLibrary extends DocumentationBuilder {

    public InstallingLibrary() {
        super("Installing Documentation testing");

        withStructureBuilder(InstallingLibrary.class,
                b -> b.getDocumentOptions(),
                b -> String.format("= %s\n", b.getDocumentTitle()),
                b -> b.getContent());
    }

    protected String getContent() {
        return "To be written";
    }

    public static void main(String... args) throws IOException {
        Document.produce(new InstallingLibrary());
    }
}
