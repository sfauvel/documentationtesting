package fr.xebia.katas.gildedrose;

import org.sfvl.doctesting.DemoDocumentation;
import org.sfvl.doctesting.Document;

import java.io.IOException;
import java.nio.file.Paths;

public class InnDocumentation extends DemoDocumentation {
    public InnDocumentation() {
        super("Gilded Rose");
    }

    public static void main(String... args) throws IOException {
        new Document(InnDocumentation.class).saveAs(Paths.get("Documentation.adoc"));
    }

}
