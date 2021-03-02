package org.sfvl.doctesting.junitextension;

import org.sfvl.doctesting.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JUnitExtensionDocumentation {

    public static void main(String... args) throws IOException {
        final ClassDocumentation generator = new ClassDocumentation();
        final String classDocumentation = generator.getClassDocumentation(ApprovalsExtensionTest.class);

        final Path docPathInProject = Paths.get("src", "test", "docs");
        final Path docRootPath = new PathProvider().getProjectPath().resolve(docPathInProject);

        final Path docFilePath = docRootPath.resolve(DocumentationNamer.toPath(JUnitExtensionDocumentation.class, "", ".adoc"));

        final DocWriter doc = new DocWriter();
        doc.write(":source-highlighter: rouge",
                ":toc: left",
                ":nofooter:",
                ":stem:",
                ":toclevels: 4",
                "",
                classDocumentation);

        try (FileWriter fileWriter = new FileWriter(docFilePath.toFile())) {
            fileWriter.write(doc.read());
        }

    }

}
