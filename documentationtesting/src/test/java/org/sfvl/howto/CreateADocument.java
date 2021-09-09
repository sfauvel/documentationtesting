package org.sfvl.howto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.HtmlPageExtension;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.OnePath;
import org.sfvl.samples.generateHtml.HtmlTest;
import org.sfvl.samples.htmlPageHeader.HtmlHeaderTest;
import org.sfvl.samples.htmlPageName.HtmlNameTest;
import org.sfvl.test_tools.OnlyRunProgrammatically;
import org.sfvl.test_tools.ProjectTestExtension;

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
    @DisplayName("Customize document options")
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
    @DisplayName("Define name and path for the output file")
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

}
