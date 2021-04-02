package org.sfvl.doctesting.writer;

import org.sfvl.doctesting.utils.DocumentationNamer;
import org.sfvl.doctesting.utils.PathProvider;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Document {
    private final String content;

    public Document(String content) {
        this.content = content;
    }

    public Document(DocumentationBuilder docBuilder) {
        this(docBuilder.build());
    }

    public Document(Class<? extends DocumentationBuilder> docBuilderClass) {
        this(createBuilderInstance(docBuilderClass).build());
    }

    public static DocumentationBuilder createBuilderInstance(Class<? extends DocumentationBuilder> docBuilderClass) {
        try {
            return docBuilderClass.getConstructor().newInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException("Exception creating builder", e);
        }
    }

    public static void produce(DocumentationBuilder docBuilder) throws IOException {
        new Document(docBuilder).saveAs(docBuilder.getClass());
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
