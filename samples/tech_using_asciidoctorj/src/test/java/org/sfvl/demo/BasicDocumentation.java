package org.sfvl.demo;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.jruby.AsciiDocDirectoryWalker;
import org.sfvl.doctesting.demo.DemoDocumentation;
import org.sfvl.doctesting.writer.Document;
import org.sfvl.doctesting.utils.PathProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.asciidoctor.Asciidoctor.Factory.create;

/**
 * Generate documentation and convert it to html using asciidoctorj.
 */
public class BasicDocumentation extends DemoDocumentation {

    public BasicDocumentation() {
        super("Asciidoctorj");
    }

    private void convertToHtml() {
        final Path outputProjectDocsPath = new PathProvider().getProjectPath().resolve(getOutputProjectPath());
        try (Asciidoctor asciidoctor = create()) {
            final AsciiDocDirectoryWalker files = new AsciiDocDirectoryWalker(getFullDocRootPath().toString());
            for (File asciidocFile : files) {
                if (asciidocFile.toString().endsWith(".adoc")) {
                    final Path outputPath = getOutputPath(outputProjectDocsPath, asciidocFile);
                    Files.createDirectories(outputPath);
                    asciidoctor.convertFile(asciidocFile,
                            OptionsBuilder.options()
                                    .safe(SafeMode.UNSAFE)
                                    .toDir(outputPath.toFile()));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Path getFullDocRootPath() {
        return new PathProvider().getProjectPath().resolve(getDocRootPath());
    }

    private Path getAbsoluteDocPath() {
        return getProjectPath().resolve(getDocRootPath());
    }

    private Path getDocRootPath() {
        return Paths.get("src", "test", "docs");
    }

    private Path getProjectPath() {
        return new PathProvider().getProjectPath();
    }

    private Path getOutputProjectPath() {
        final Path projectPath = new PathProvider().getProjectPath();
        final Path projectFolder = projectPath.getFileName();
        return Paths.get("target", "docs").resolve(projectFolder);
    }

    private Path getOutputPath(Path outputPath, File asciidocFile) throws IOException {
        final Path relativizeToRootPath = getFullDocRootPath().relativize(asciidocFile.toPath());
        final Path outputFile = outputPath.resolve(relativizeToRootPath);
        return outputFile.getParent();
    }

    public static void main(String... args) throws IOException {
        final BasicDocumentation generator = new BasicDocumentation();
        new Document(generator.build()).saveAs(Paths.get("index.adoc"));
        generator.convertToHtml();
    }
}
