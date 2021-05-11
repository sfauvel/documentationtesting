package org.sfvl.doctesting.writer;

import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocumentationNamer;
import org.sfvl.doctesting.utils.OnePath;
import org.sfvl.doctesting.utils.PathProvider;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

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
        final Path resolve = Config.DOC_PATH.resolve(outputFile);
        extracted(resolve);
    }

    public void saveAs(OnePath outputFile) throws IOException {
        final Path path = outputFile.path();
        extracted(path);
    }

    private void extracted(Path resolve) throws IOException {
        final Path docFilePath = new PathProvider()
                .getProjectPath()
                .resolve(resolve);

        try (FileWriter fileWriter = new FileWriter(docFilePath.toFile())) {
            fileWriter.write(this.content);
        }
    }

}
