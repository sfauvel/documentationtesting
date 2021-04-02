package org.sfvl.doctesting.junitextension;

import org.sfvl.doctesting.writer.Document;
import org.sfvl.doctesting.writer.DocumentationBuilder;

import java.io.IOException;

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
