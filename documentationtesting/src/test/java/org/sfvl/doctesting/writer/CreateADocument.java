package org.sfvl.doctesting.writer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.utils.CodeExtractor;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.OnePath;
import org.sfvl.doctesting.writer.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * DocumentationBuilder provides an easy way to create a document.
 * It's useful to creates pages with a free structure in which we can select files to include.
 */
public class CreateADocument {

    private AsciidocFormatter formatter = new AsciidocFormatter();

    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();


    private Path includeFromTo(Object fromClass, OnePath to) {
        final DocPath from = new DocPath(fromClass.getClass());
        return from.approved().folder().relativize(to.path());
    }


    /**
     * Creating a document is just writing a file.
     * We can use a link:https://en.wikipedia.org/wiki/Markup_language[markup language] to easily writing a formatted document.
     *
     * @param testInfo
     * @throws IOException
     */
    @Test
    public void simplest_way_to_create_a_document(TestInfo testInfo) throws IOException {
        final OnePath outputFile = new DocPath(DocumentVerySimple.class).approved();

        // >>>1
        class MyDocument {
            protected String content() {
                return String.join("\n",
                        "= My title",
                        "Text of the document"
                );
            }
        }

        new Document(new MyDocument().content()).saveAs(outputFile);
        // <<<1

        writeDoc(testInfo, outputFile);
    }

    /**
     * To build a document, we can reuse some code that generate a part of document.
     * To do that, we can use:
     *
     * * a method in the same class
     * * use another class
     * * a lambda
     *
     * @param testInfo
     * @throws IOException
     */
    @Test
    public void reuse_sub_parts_of_document(TestInfo testInfo) throws IOException {
        final OnePath outputFile = new DocPath(DocumentWithSubPart.class).approved();

        // >>>1
        class Header {
            private final Formatter formatter;

            public Header(Formatter formatter) {
                this.formatter = formatter;
            }

            public String content() {
                return formatter.paragraph(
                        ":toc: left",
                        ":nofooter:",
                        ":stem:"
                );
            }
        }

        class MyDocument {
            private Formatter formatter = new AsciidocFormatter();

            private Function<Formatter, String> title = f -> f.title(1, "My title");

            private String description() {
                return "Text of the document";
            }

            protected String content() {
                return String.join("\n",
                        // Using another class.
                        new Header(formatter).content(),
                        // Using a lambda
                        title.apply(formatter),
                        // Using a method in the same class.
                        description()
                );
            }
        }

        new Document(new MyDocument().content()).saveAs(outputFile);
        // <<<1

        writeDoc(testInfo, outputFile);
    }

    public void writeDoc(TestInfo testInfo, String content) {
        final String view_rendering = content.replaceAll("(\n|^)=(.*)", "$1==$2");
        writeDoc(testInfo, content, view_rendering);
    }

    private void writeDoc(TestInfo testInfo, OnePath outputFile) throws IOException {
        final String content = Files.lines(outputFile.path())
                .collect(Collectors.joining("\n"));
        final String view_rendering = formatter.include(outputFile.from(this.getClass()).toString(), 1);

        writeDoc(testInfo, content, view_rendering);
    }

    public void writeDoc(TestInfo testInfo, String content, String view_rendering) {
        doc.write("", ".Usage", "[source, java, indent=0]",
                "----",
                CodeExtractor.extractPartOfMethod(testInfo.getTestMethod().get(), "1"),
                "----",
                "");

        writeColumns(
                String.join("\n",
                        ".Document generated",
                        "----",
                        content.replaceAll("\\ninclude", "\n\\\\include"),
                        "----"),
                String.join("\n",
                        "_final rendering_",
                        "[.adocRendering]",
                        view_rendering));

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
        doc.write("", style, "");
    }

    public void writeColumns(String... cells) {
// Title in a table not working very well. There is a warning when generating HTML.
//        doc.write(String.format("[cols=%d]", cells.length),
//                "|====",
//                Arrays.stream(cells).map(text -> String.format("a|%s", text)).collect(Collectors.joining("\n")),
//                "|====");

        doc.write("", "");
        doc.write(Arrays.stream(cells).collect(Collectors.joining("\n")));
    }

}

class DocumentVerySimple {
}

class DocumentWithSubPart {
}

