package org.sfvl.demo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test just write a doc.
 * If there is modified files in docs folder, there is a regression.
 */
public class DemoTest {

    static final Path docPath = Paths.get("src", "test", "docs");

    @BeforeAll
    static public void init() throws IOException {
        Files.createDirectories(docPath);
    }

    // tag::test[]
    @Test
    public void should_be_5_when_adding_2_and_3() throws IOException {
        final Path filePath = docPath.resolve("_DemoTest.adoc");
        final FileWriter fileWriter = new FileWriter(filePath.toFile().toString());
        try (BufferedWriter writer = new BufferedWriter(fileWriter)) {

            int a = 2;
            int b = 3;

            final String output = String.join("\n",
                    "= Should add 2 numbers",
                    "",
                    String.format("%d + %d = *%d*", a, b, a + b));

            writer.write(output);
        }
    }
    // end::test[]

}
