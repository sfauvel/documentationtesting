package org.sfvl.demo;

import org.sfvl.doctesting.ClassDocumentation;
import org.sfvl.doctesting.DemoDocumentation;
import org.sfvl.doctesting.DocumentationNamer;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BasicDocumentation extends DemoDocumentation {

    public BasicDocumentation() {
        super("JUnit5 extension");
    }

    public static void generateClassDoc(BasicDocumentation generator, Class<DemoTest> classToDocument) throws IOException {
        final Path docRootPath = Paths.get("src", "test", "docs");
        final ClassDocumentation classDocumentation = new ClassDocumentation();

        final Path path = docRootPath.resolve(DocumentationNamer.toPath(DemoTest.class, "", ".adoc"));
        try (FileWriter writer = new FileWriter(path.toFile())) {
            writer.write(classDocumentation.getClassDocumentation(classToDocument));
        }
    }

    public static void main(String... args) throws IOException {
        final BasicDocumentation generator = new BasicDocumentation();

        generateClassDoc(generator, DemoTest.class);

        generator.generate("org.sfvl");
    }


}
