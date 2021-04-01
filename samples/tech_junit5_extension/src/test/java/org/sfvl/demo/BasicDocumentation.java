package org.sfvl.demo;

import org.sfvl.doctesting.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BasicDocumentation extends DemoDocumentation {

    public BasicDocumentation() {
        super("JUnit5 extension");
    }

    public static void generateClassDoc(Class<DemoTest> classToDocument) throws IOException {
        final Path docRootPath = new PathProvider().getProjectPath().resolve(Paths.get("src", "test", "docs"));
        final ClassDocumentation classDocumentation = new ClassDocumentation();

        final Path path = docRootPath.resolve(DocumentationNamer.toPath(DemoTest.class, "", ".adoc"));
        try (FileWriter writer = new FileWriter(path.toFile())) {
            writer.write(classDocumentation.getClassDocumentation(classToDocument));
        }
    }

    public static void main(String... args) throws IOException {
        generateClassDoc(DemoTest.class);
        new Document(BasicDocumentation.class).saveAs(Paths.get("Documentation.adoc"));
    }


}
