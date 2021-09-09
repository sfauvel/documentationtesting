package org.sfvl.howto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.HtmlPageExtension;
import org.sfvl.doctesting.utils.CodeExtractor;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.OnePath;
import org.sfvl.doctesting.writer.Document;
import org.sfvl.samples.generateHtml.HtmlTest;
import org.sfvl.samples.htmlPageHeader.HtmlHeaderTest;
import org.sfvl.samples.htmlPageName.HtmlNameTest;
import org.sfvl.test_tools.OnlyRunProgrammatically;
import org.sfvl.test_tools.ProjectTestExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * DocumentationBuilder provides an easy way to create a document.
 * It's useful to creates pages with a free structure in which we can select files to include.
 */
public class CreateADocument {

    private AsciidocFormatter formatter = new AsciidocFormatter();

    @RegisterExtension
    static ProjectTestExtension doc = new ProjectTestExtension();


    private Path includeFromTo(Object fromClass, OnePath to) {
        final DocPath from = new DocPath(fromClass.getClass());
        return from.approved().folder().relativize(to.path());
    }

    /**
     * :underscore: _
     *
     * To convert `.adoc` to `.html`, we use `asciidoctor-maven-plugin` plugin.
     * By default, files started with `{underscore}` are not converted to HTML.
     *
     * All the `approved` files we generate start with `{underscore}`.
     * They are only chapters that need to be include in a file to be converted into HTML.
     * So, it's easier to reuse these files and organize the documentation.
     *
     * To have a file that not start with `{underscore}`, we need to generate it.
     * We can do that in a method annoted with `AfterAll`.
     * In this file, we can add `include` to all files we want to aggregate.
     *
     * If you just want to publish the documentation created in a test class,
     * you can create a file with the name of the class (without {underscore})
     * and add an `include` to the `approved` file of the same class.
     */
    @Test
    public void generate_html() {
        final Class<?> testClass = HtmlTest.class;
        final DocPath docPath = new DocPath(testClass);

        doc.runTestAndWriteResultAsComment(testClass);

        final String source = getLines(docPath.test().path())
                .filter(line -> !line.contains(NotIncludeToDoc.class.getSimpleName()))
                .filter(line -> !line.contains(OnlyRunProgrammatically.class.getSimpleName()))
                .collect(Collectors.joining("\n"));

        final Path docFolder = docPath.approved().folder();

        final String filesInDocFolder;
            filesInDocFolder = getFiles(docFolder)
                    .map(f -> "* " + f.getFileName().toString())
                    .sorted()
                    .collect(Collectors.joining("\n"));


        doc.write(".Example of class creating a file to convert into HTML", formatter.sourceCode(source));

        doc.write("", "",
                String.format("Files in folder `%s`", DocPath.toAsciiDoc(docFolder)),
                "",
                filesInDocFolder);

        final Path path = docPath.page().path();
        final String contentOfGeneratedFile = getLines(path).collect(Collectors.joining("\n"));

        doc.write("", "",
                String.format(".Content of the file `%s`", DocPath.toAsciiDoc(path)),
                formatter.blockBuilder("----")
                        .escapeSpecialKeywords()
                        .content(contentOfGeneratedFile).build());

    }

    @Test
    public void generate_header_html() {
        final Class<?> testClass = HtmlHeaderTest.class;
        final DocPath docPath = new DocPath(testClass);

        doc.runTestAndWriteResultAsComment(testClass);

        final String source = getLines(docPath.test().path())
                .filter(line -> !line.contains(NotIncludeToDoc.class.getSimpleName()))
                .filter(line -> !line.contains(OnlyRunProgrammatically.class.getSimpleName()))
                .collect(Collectors.joining("\n"));

        final Path path = docPath.page().path();
        final String contentOfGeneratedFile = getLines(path).collect(Collectors.joining("\n"));

        doc.write(String.format("By default, `%s` create a file with only one include of the `approved` class file.", HtmlPageExtension.class.getSimpleName()),
                "This file is the right place to specify some specific information on how displaying the page.",
                String.format("We can doing it extending `%s` and redefined content to add options we need.", HtmlPageExtension.class.getSimpleName()),
                "Here, we create an inner class but we can use a main class to reuse it in several tests.",
                "", "");

        doc.write(".Example to add header into file to convert into HTML", formatter.sourceCode(source));

        doc.write("", "",
                String.format(".Content of the file `%s`", DocPath.toAsciiDoc(path)),
                formatter.blockBuilder("----")
                        .escapeSpecialKeywords()
                        .content(contentOfGeneratedFile).build());
    }

    @Test
    public void change_name_for_html() {
        final Class<?> testClass = HtmlNameTest.class;
        final DocPath docPath = new DocPath(testClass);

        doc.runTestAndWriteResultAsComment(testClass);

        final String source = getLines(docPath.test().path())
                .filter(line -> !line.contains(NotIncludeToDoc.class.getSimpleName()))
                .filter(line -> !line.contains(OnlyRunProgrammatically.class.getSimpleName()))
                .collect(Collectors.joining("\n"));

        doc.write(String.format("By default, `%s` create a file with a name coming from the class", HtmlPageExtension.class.getSimpleName()),
                String.format("To change it, we can extending `%s` and redefined name method.", HtmlPageExtension.class.getSimpleName()),
                "", "");

        final Path docFolder = docPath.approved().folder();

        final String filesInDocFolder;
        filesInDocFolder = getFiles(docFolder)
                .map(f -> "* " + f.getFileName().toString())
                .sorted()
                .collect(Collectors.joining("\n"));

        doc.write(".Example of class creating a file to convert into HTML", formatter.sourceCode(source));

        doc.write("", "",
                String.format("Files in folder `%s`", DocPath.toAsciiDoc(docFolder)),
                "",
                filesInDocFolder);


    }


    private Stream<Path> getFiles(Path docFolder) {
        try {
            return Files.list(docFolder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Stream<String> getLines(Path path) {
        try {
            return Files.lines(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
