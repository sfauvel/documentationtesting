package org.sfvl.howto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.utils.CodeExtractor;
import org.sfvl.doctesting.utils.DocWriter;
import org.sfvl.doctesting.utils.DocumentationNamer;
import org.sfvl.doctesting.writer.Document;
import org.sfvl.doctesting.writer.DocumentationBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * DocumentationBuilder provides an easy way to create a document.
 * It's useful to creates pages with a free structure in which we can select files to include.
 */
public class CreateADocument {

    private static final DocWriter doc = new DocWriter();
    @RegisterExtension
    static ApprovalsExtension extension = new ApprovalsExtension(doc);

    private final AsciidocFormatter formatter = new AsciidocFormatter();

    @Test
    public void simplest_way_to_create_a_document(TestInfo testInfo) throws IOException {

        final Path outputFile = DocumentationNamer
                .toPath(this.getClass().getPackage())
                .resolve("DocumentSample.adoc");

        // >>>1
        DocumentationBuilder builder = new DocumentationBuilder("My title") {
            @Override
            protected String getContent() {
                return "Text of the document";
            }
        };

        new Document(builder).saveAs(outputFile);
        // <<<1

        writeDoc(testInfo, outputFile);
    }

    public void writeDoc(TestInfo testInfo, String content) {
        doc.write("", ".Usage", "[source, java, indent=0]",
                "----",
                CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get(), "1"),
                "----",
                "");

        doc.write("", ".Document generated",
                "----",
                content.replaceAll("\\ninclude", "\n\\\\include"),
                "----");

        String style = "++++\n" +
                "<style>\n" +
                ".adocRendering {\n" +
                "    padding: 1em;\n" +
                "    background: #fffef7;\n" +
                "    border-color: #e0e0dc;\n" +
                "    -webkit-box-shadow: 0 1px 4px #e0e0dc;\n" +
                "    box-shadow: 0 1px 4px #e0e0dc;\n" +
                "}\n" +
                "</style>\n" +
                "++++";
        doc.write("", "", style, "", "_final rendering_", "[.adocRendering]",
                content.replaceAll("(\n|^)=(.*)", "$1==$2")
        );
    }
    public void writeDoc(TestInfo testInfo, Path file) throws IOException {
        doc.write("", ".Usage", "[source, java, indent=0]",
                "----",
                CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get(), "1"),
                "----",
                "");

        doc.write("", ".Document generated",
                "----",
                Files.lines(Paths.get("src", "test", "docs").resolve(file))
                        .collect(Collectors.joining("\n"))
                        .replaceAll("\\ninclude", "\n\\\\include"),
                "----");


        String style = "++++\n" +
                "<style>\n" +
                ".adocRendering {\n" +
                "    padding: 1em;\n" +
                "    background: #fffef7;\n" +
                "    border-color: #e0e0dc;\n" +
                "    -webkit-box-shadow: 0 1px 4px #e0e0dc;\n" +
                "    box-shadow: 0 1px 4px #e0e0dc;\n" +
                "}\n" +
                "</style>\n" +
                "++++";

        final Path relativize = DocumentationNamer.toPath(this.getClass().getPackage()).relativize(file);

        doc.write("", "", style, "", "_final rendering_", "[.adocRendering]",
                "include::" + relativize + "[leveloffset=+1]"
        );
    }

}
