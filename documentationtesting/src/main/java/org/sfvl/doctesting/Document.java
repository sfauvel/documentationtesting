package org.sfvl.doctesting;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Document {
    private final String content;

    public Document(String content) {
        this.content = content;
    }

    public static void produce(DocumentationBuilder docBuilder) throws IOException {
        new Document(docBuilder.build()).saveAs(docBuilder.getClass());
    }

    public void saveAs(Class<?> aClass) throws IOException {
        saveAs(DocumentationNamer.toAsciiDocFilePath(aClass));
    }

    public void saveAs(Path outputFile) throws IOException {
        final Path docFilePath = new PathProvider()
                .getProjectPath()
                .resolve(Paths.get("src", "test", "docs"))
                .resolve(outputFile);

        try (FileWriter fileWriter = new FileWriter(docFilePath.toFile())) {
            fileWriter.write(this.content);
        }
    }

    static Document buildFrom(DocumentationBuilder builder) {
        return new Document(builder.build());
    }
}
