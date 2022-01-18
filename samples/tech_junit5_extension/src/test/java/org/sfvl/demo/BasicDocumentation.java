package org.sfvl.demo;

import org.sfvl.codeextraction.CodeExtractor;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.demo.DemoDocumentation;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.PathProvider;
import org.sfvl.doctesting.writer.ClassDocumentation;
import org.sfvl.doctesting.writer.Document;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class BasicDocumentation extends DemoDocumentation {

    public BasicDocumentation() {
        super("JUnit5 extension");
    }

    public static void generateClassDoc(Class<DemoTest> classToDocument) throws IOException {
        final ClassDocumentation classDocumentation = new ClassDocumentation(new AsciidocFormatter());

        final Path approvedPath = new DocPath(DemoTest.class).approved().path();
        final Path absoluteApprovedPath = new PathProvider().getProjectPath().resolve(approvedPath);
        try (FileWriter writer = new FileWriter(absoluteApprovedPath.toFile())) {
            writer.write(classDocumentation.getClassDocumentation(classToDocument));
        }
    }

    @Override
    public void produce() throws IOException {
        new Document(this.build()).saveAs(Config.DOC_PATH.resolve("index.adoc"));
    }

    public static void main(String... args) throws IOException {
        CodeExtractor.init(Config.TEST_PATH, Config.SOURCE_PATH);
        generateClassDoc(DemoTest.class);
        new BasicDocumentation().produce();
    }

}
