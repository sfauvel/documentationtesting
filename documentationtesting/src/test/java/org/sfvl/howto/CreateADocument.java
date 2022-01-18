package org.sfvl.howto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.codeextraction.CodeExtractor;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.HtmlPageExtension;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.OnePath;
import org.sfvl.samples.generateHtml.HtmlTest;
import org.sfvl.samples.generateNestedHtml.HtmlNestedTest;
import org.sfvl.samples.generateOnlyNestedHtml.HtmlOnlyNestedTest;
import org.sfvl.samples.htmlPageConstructor.HtmlNameConstructorTest;
import org.sfvl.samples.htmlPageHeader.HtmlHeaderTest;
import org.sfvl.samples.htmlPageName.HtmlNameTest;
import org.sfvl.test_tools.OnlyRunProgrammatically;
import org.sfvl.test_tools.ProjectTestExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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

        final String source = sourceCodeLines(testClass);

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
        doc.write("With nested class, only class with direct extension generate a page", "");
        final Class<?> testClass = HtmlNestedTest.class;
        final DocPath docPath = new DocPath(testClass);

        doc.removeNonApprovalFiles(docPath);
        doc.runTestAndWriteResultAsComment(testClass);

        final String source = sourceCodeLines(testClass);

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
    public void generate_html_only_on_nested_class() {
        doc.write("It's not possible to generate a page on a nested class.",
                "A nested class do not have is own file for the class.",
                "Every methods and classes are included in the approved file for the java file.",
                "");
        final Class<?> testClass = HtmlOnlyNestedTest.class;
        final DocPath docPath = new DocPath(testClass);

        doc.removeNonApprovalFiles(docPath);
        doc.runTestAndWriteResultAsComment(testClass);

        final String source = sourceCodeLines(testClass);

        final Path docFolder = docPath.approved().folder();

        final String filesInDocFolder;
        filesInDocFolder = getFiles(docFolder)
                .map(f -> "* " + f.getFileName().toString())
                .sorted()
                .collect(Collectors.joining("\n"));


        doc.write(".Example of class with extension on nested class", formatter.sourceCode(source));

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

        final String source = sourceCodeLines(testClass);

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

        final String source = sourceCodeLines(testClass);

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

    @Test
    @DisplayName("Define name and path for the output file with constructor")
    public void change_name_for_html_using_constructor() {
        final Class<?> testClass = HtmlNameConstructorTest.class;
        final DocPath docPath = new DocPath(testClass);

        doc.removeNonApprovalFiles(docPath);
        doc.runTestAndWriteResultAsComment(testClass);

        final String source = sourceCodeLines(testClass);
//        final String source = sourceCodeLines(testClass);

        doc.write(String.format("You can create an `%s` giving the file name to generate.", HtmlPageExtension.class.getSimpleName()),
                "The name should not contain the extension.",
                "It will be added to the given name.",
                "The file is placed from the root of the project.",
                "It's an easy way to generate the index file of the project.",
                "", "");

        final Path docFolder = docPath.approved().folder();

        final String filesInDocFolder;
        filesInDocFolder = getFiles(docFolder)
                .map(f -> "* " + f.getFileName().toString())
                .sorted()
                .collect(Collectors.joining("\n"));

        doc.write(".Example of class creating a named file to convert into HTML", formatter.sourceCodeBuilder("java").source(source).build(), "");

        doc.write(String.format("Files in folder `%s`", DocPath.toAsciiDoc(docFolder)),
                "",
                filesInDocFolder);


    }

    private String sourceCodeLines(Class<?> testClass) {
        return cleanSourceLines(Arrays.stream(CodeExtractor.classSource(testClass).split("\n")));
    }

    private String sourceCodeLines(DocPath docPath) {
        return cleanSourceLines(getLines(docPath.test().path()));
    }

    private String cleanSourceLines(Stream<String> lines) {
        return lines
                .filter(line -> !line.contains(NotIncludeToDoc.class.getSimpleName()))
                .filter(line -> !line.contains(OnlyRunProgrammatically.class.getSimpleName()))
                .collect(Collectors.joining("\n"));
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
