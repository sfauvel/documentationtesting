package org.sfvl.demo;

import org.sfvl.doctesting.ClassDocumentation;
import org.sfvl.doctesting.DemoDocumentation;
import org.sfvl.doctesting.DocumentationNamer;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class BasicDocumentation extends DemoDocumentation {

    public BasicDocumentation() {
        super("JUnit5 extension");
    }

    public static void generateClassDoc(BasicDocumentation generator, Class<DemoTest> classToDocument) throws IOException {
        final ClassDocumentation classDocumentation = new ClassDocumentation();

        final Path path = generator.getDocRootPath().resolve(DocumentationNamer.toPath(DemoTest.class, "", ".adoc"));
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
