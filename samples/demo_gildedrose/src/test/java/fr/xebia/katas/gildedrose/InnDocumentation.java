package fr.xebia.katas.gildedrose;

import org.sfvl.doctesting.demo.DemoDocumentation;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.writer.Document;

import java.io.IOException;
import java.nio.file.Paths;

public class InnDocumentation extends DemoDocumentation {
    public InnDocumentation() {
        super("Gilded Rose");
    }

    @Override
    public void produce() throws IOException {
        new Document(this.build()).saveAs(Config.DOC_PATH.resolve("index.adoc"));
    }

    public static void main(String... args) throws IOException {
        new InnDocumentation().produce();
    }

}
