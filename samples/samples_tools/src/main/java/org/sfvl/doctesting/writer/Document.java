package org.sfvl.doctesting.writer;

import org.sfvl.doctesting.utils.*;

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
        saveAs(new DocPath(aClass).page());
    }

    public void saveAs(Path outputFile) throws IOException {
        extracted(outputFile);
    }

    public void saveAs(OnePath outputFile) throws IOException {
        extracted(outputFile.path());
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
