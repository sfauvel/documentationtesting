package org.sfvl.doctesting.junitextension;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.sfvl.doctesting.utils.DocPath;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class HtmlPageExtension implements AfterAllCallback {

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        generate(extensionContext.getTestClass().get());
    }

    public void generate(Class<?> clazz) {
        final Path path = getFilePath(clazz);
        try (FileWriter fileWriter = new FileWriter(path.toFile())) {
            fileWriter.write(content(clazz));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path getFilePath(Class<?> clazz) {
        return new DocPath(clazz).page().path();
    }

    public String content(Class<?> clazz) {
        return String.format("include::%s[]", new DocPath(clazz).approved().from(getFilePath(clazz).getParent()));
    }

}
