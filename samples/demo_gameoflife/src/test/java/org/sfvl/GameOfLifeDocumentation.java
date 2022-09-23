package org.sfvl;

import org.sfvl.doctesting.demo.DemoDocumentation;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.writer.Document;

import java.io.IOException;

public class GameOfLifeDocumentation extends DemoDocumentation {

    public GameOfLifeDocumentation() {
        super("Game of life");
    }

    static String include_style() {
        return String.join("\n",
                "ifndef::STYLE_INCLUDED[]",
                "include::{ROOT_PATH}/../resources/style.css[]",
                ":STYLE_INCLUDED:",
                "endif::STYLE_INCLUDED[]");
    }

    @Override
    public String build() {
        return super.build() + "\n" + include_style();
    }

    @Override
    public void produce() throws IOException {
        new Document(this.build()).saveAs(Config.DOC_PATH.resolve("index.adoc"));
    }

    public static void main(String... args) throws IOException {
        new GameOfLifeDocumentation().produce();
    }

}
