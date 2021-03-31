package org.sfvl.doctesting.junitextension;

import org.sfvl.doctesting.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JUnitExtensionDocumentation extends DocumentationBuilder {

    public JUnitExtensionDocumentation() {
        super("Approvals extension");
        withLocation(JUnitExtensionDocumentation.class.getPackage());
        withClassesToInclude(ApprovalsExtensionTest.class);
        withOptionAdded("source-highlighter", "rouge");
        withOptionAdded("toclevels", "4");
        withStructureBuilder(JUnitExtensionDocumentation.class,
                b -> b.getDocumentOptions(),
                b -> b.includeClasses());
    }
    public static void main(String... args) throws IOException {
        Document.produce(new JUnitExtensionDocumentation());
    }

}
