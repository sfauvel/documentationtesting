package org.sfvl.doctesting.junitextension;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.sfvl.doctesting.utils.DocPath;

import java.io.FileWriter;

public class HtmlPageExtension implements AfterAllCallback {

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        final DocPath docPath = new DocPath(extensionContext.getTestClass().get());

        String includeContent = content(docPath);

        try (FileWriter fileWriter = new FileWriter(docPath.page().path().toFile())) {
            fileWriter.write(includeContent);
        }
    }

    public String content(DocPath docPath) {
        String includeContent = header();
        if (!includeContent.isEmpty()) {
            includeContent += "\n";
        }
        includeContent += String.format("include::%s[]", docPath.approved().fullname());
        return includeContent;
    }

    public String header() {
        return "";
    }


}
