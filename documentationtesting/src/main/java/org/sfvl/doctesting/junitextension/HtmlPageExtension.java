package org.sfvl.doctesting.junitextension;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.sfvl.doctesting.utils.DocPath;

import java.io.FileWriter;

public class HtmlPageExtension implements AfterAllCallback {

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        final DocPath docPath = new DocPath(extensionContext.getTestClass().get());

        String includeContent = String.join("\n",
                String.format("include::%s[]", docPath.approved().fullname()));

        try (FileWriter fileWriter = new FileWriter(docPath.page().path().toFile())) {
            fileWriter.write(includeContent);
        }
    }

}
