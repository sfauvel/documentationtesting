package org.sfvl.demo;

import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.writer.Document;
import org.sfvl.doctesting.writer.ClassDocumentation;
import org.sfvl.doctesting.demo.DemoDocumentation;
import org.sfvl.doctesting.utils.DocumentationNamer;
import org.sfvl.doctesting.utils.PathProvider;

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

        final Path path = docRootPath.resolve(DocumentationNamer.toPath(DemoTest.class, "", ".approved.adoc"));
        try (FileWriter writer = new FileWriter(path.toFile())) {
            writer.write(classDocumentation.getClassDocumentation(classToDocument));
        }
    }

    @Override
    public void produce() throws IOException {
        new Document(this.build()).saveAs(Config.DOC_PATH.resolve("Documentation.adoc"));
    }

    public static void main(String... args) throws IOException {
        generateClassDoc(DemoTest.class);
        new BasicDocumentation().produce();
    }

}
