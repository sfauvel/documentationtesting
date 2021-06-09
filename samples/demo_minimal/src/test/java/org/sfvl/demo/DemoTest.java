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

    @Test
    public void should_be_5_when_adding_2_and_3() throws IOException {
        final Path filePath = docPath.resolve("index.adoc");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile().toString()))) {

            int a = 2;
            int b = 3;

            writer.write(":nofooter:");
            writer.newLine();
            writer.write("= Should add 2 numbers");
            writer.newLine();
            writer.newLine();
            writer.write(String.format("%d + %d = %d", a, b, a+b));
        }

    }

}
