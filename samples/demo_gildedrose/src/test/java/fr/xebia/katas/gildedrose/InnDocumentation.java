package fr.xebia.katas.gildedrose;

import org.sfvl.doctesting.DemoDocumentation;

import java.io.IOException;

public class InnDocumentation extends DemoDocumentation {
    public InnDocumentation() {
        super("Gilded Rose");
    }

    public static void main(String... args) throws IOException {
        final InnDocumentation generator = new InnDocumentation();

        generator.generate("fr.xebia.katas");
    }

}
