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

    public String getContent() {
        return content;
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

}
