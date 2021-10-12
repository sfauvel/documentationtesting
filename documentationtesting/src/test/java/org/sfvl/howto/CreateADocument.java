package org.sfvl.howto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.HtmlPageExtension;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.OnePath;
import org.sfvl.samples.generateHtml.HtmlTest;
import org.sfvl.samples.generateNestedHtml.HtmlNestedTest;
import org.sfvl.samples.htmlPageHeader.HtmlHeaderTest;
import org.sfvl.samples.htmlPageName.HtmlNameTest;
import org.sfvl.test_tools.OnlyRunProgrammatically;
import org.sfvl.test_tools.ProjectTestExtension;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * We call `document` one page of the documentation which corresponds to an HTML page.
 * These `documents` are converted from the `.adoc` files.
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
     * It's configure in `pom.xml` and it's can be run in one phase of the maven lifecycle (generally the `package` phase).
     *
     * By default, files started with `{underscore}` are not converted to HTML by this plugin.
     * We have chosen to start all `approved` file names with an `{underscore}`.
     * They are only chapters of documents.
     * They need to be included in a file that will be converted into HTML.
     * So, this makes it easier to reuse these chapters and organize the documentation.
     */
    @Test
    public void generate_html() {
        doc.write("To have a file that not start with `{underscore}`, we need to generate one.",
                String.format("The `%s` extension is made for that.", HtmlPageExtension.class.getSimpleName()),
                "When a test class used this extension, it will generate a simple `.adoc` file that include the `approved` file of the same class.",
                "The final HTML file will contain all the chapters generated from methods of this class.",
                ""
        );
        final Class<?> testClass = HtmlTest.class;
        final DocPath docPath = new DocPath(testClass);

        doc.removeNonApprovalFiles(docPath);
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


        doc.write(".Example of class creating the file that will be converted into HTML", formatter.sourceCode(source));

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
    public void generate_html_with_nested_class() {
        doc.write("With nested class, only class with direct extension generate a page");
        final Class<?> testClass = HtmlNestedTest.class;
        final DocPath docPath = new DocPath(testClass);

        doc.removeNonApprovalFiles(docPath);
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


        doc.write(".Example of class creating the file that will be converted into HTML", formatter.sourceCode(source));

        doc.write("", "",
                String.format("Files in folder `%s`", DocPath.toAsciiDoc(docFolder)),
                "",
                filesInDocFolder);


    }

    @Test
    @DisplayName("Customize document options")
    public void generate_header_html() {
        final Class<?> testClass = HtmlHeaderTest.class;
        final DocPath docPath = new DocPath(testClass);

        doc.removeNonApprovalFiles(docPath);
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
    @DisplayName("Define name and path for the output file")
    public void change_name_for_html() {
        final Class<?> testClass = HtmlNameTest.class;
        final DocPath docPath = new DocPath(testClass);

        doc.removeNonApprovalFiles(docPath);
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

        doc.write(".Example of class creating a file to convert into HTML", formatter.sourceCodeBuilder("java").source(source).build(), "");

        doc.write(String.format("Files in folder `%s`", DocPath.toAsciiDoc(docFolder)),
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

}
