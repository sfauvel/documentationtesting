package org.sfvl.docformatter;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.codeextraction.CodeExtractor;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.utils.ClassToDocument;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.writer.DocWriter;
import org.sfvl.test_tools.IntermediateHtmlPage;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Each section of this documentation explain one method of the `AsciidocFormatter`.
 * For each method, we describe:
 *
 * - how it can be uses in code
 * - what the asciidoc generated will look like at the end (except when it's not possible)
 * - the asciidoc text generated
 */
@DisplayName("Asciidoctor formatter")
@ClassToDocument(clazz = AsciidocFormatter.class)
@ExtendWith(IntermediateHtmlPage.class)
public class AsciidocFormatterTest {
    private static AsciidocFormatter formatter = new AsciidocFormatter();
    private String output;

    @RegisterExtension
    static ApprovalsExtension doc = new ApprovalsExtension<DocWriter<Formatter>, Formatter>(new DocWriter(Config.FORMATTER) {

        @Override
        public String defineDocPath(Path relativePathToRoot) {
            return String.join("\n",
                    super.defineDocPath(relativePathToRoot),
                    "ifdef::is-html-doc[:imagesdir: {ROOT_PATH}/images]",
                    "ifndef::is-html-doc[:imagesdir: {ROOT_PATH}/../resources/images]"
            );

        }
    });

    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestOption {
        /// False if 'render' section should not be added to the final document.
        boolean showRender() default true;

        /// The name of the method in AsciidocFormatter that used to get documentation.
        String includeMethodDoc() default "";

        /// False to have generic output. True to have output of a normal test.
        boolean normalOutput() default false;

        /**
         * When true (default value) all code of the method is extracted.
         * Otherwise, only the code between >>> and <<< is extracted.
         */
        boolean extractAll() default true;
    }

    @TestOption
    private static class DefaultOption {
    }

    final static TestOption DEFAULT_OPTION = DefaultOption.class.getAnnotation(TestOption.class);


    @Test
    @DisplayName("Standard options")
    @TestOption(showRender = false, includeMethodDoc = "standardOptions")
    public void should_format_standard_option() {
        output = formatter.standardOptions();
    }

    @Test
    @DisplayName("Table of content")
    @TestOption(showRender = false)
    public void should_format_table_of_content() {
        output = formatter.tableOfContent(3);
    }

    @Test
    @DisplayName("Title")
    @TestOption(showRender = false)
    public void should_format_title() {
        output = formatter.title(2, "Description");
    }

    /**
     * Each text is written in a separate line on asciidoc file.
     * There is no line break when text is rendered in HTML.
     */
    @Test
    @DisplayName("Paragraph")
    public void should_format_paragraph() {
        output = formatter.paragraph("We write some sentences.",
                "Each of them are in is own line in asciidoc text.",
                "They are in a same paragraph at the end.");
    }

    /**
     * Join paragraph with enough line break to separate them.
     */
    @Test
    @DisplayName("Suite of paragraphs")
    public void should_format_suite_of_paragraphs() {
        output = formatter.paragraphSuite("My first paragraph.",
                "The second paragraph with a blank line before to separate from the first one.");
    }

    @Nested
    public class Style {
        @Test
        public void bold() {
            output = formatter.bold("bold text");
        }

        @Test
        public void italic() {
            output = formatter.italic("italic text");
        }
    }


    @Test
    @DisplayName("Description")
    public void should_format_description() {
        output = formatter.description("Describe something.");
    }

    @Test
    @DisplayName("Definition")
    public void should_format_definition() {
        output = formatter.addDefinition("Asciidoctor", "A fast text processor & publishing toolchain for converting AsciiDoc to HTML");
    }

    @Nested
    class Link {
        @Test
        @DisplayName("Link to page")
        public void should_format_pageLink() {
            output = formatter.linkToPage("AsciidocFormatterTest.html", "This is a link to a page");
        }

        @Test
        @DisplayName("Link to page with anchor")
        public void should_format_pageLink_with_anchor() {
            output = formatter.linkToPage("AsciidocFormatterTest.html", "AnchorExample", "This is a link to a page");
        }

        @Test
        @DisplayName("Link to local anchor")
        public void should_format_anchorLink() {
            output = formatter.linkToAnchor("AnchorExample", "This is a link to anchor in this page");
        }

        @Test
        @DisplayName("Anchor")
        public void should_format_anchor() {
            output = formatter.anchor("AnchorExample") + "You can make an anchor to here";
        }

    }

    @Nested
    @DisplayName(value = "List")
    class AsciiDocList {
        @Test
        @DisplayName("One list item")
        public void should_format_one_list_item() {
            output = formatter.listItem("First")
                    + formatter.listItem("Second");
        }

        @Test
        @DisplayName("Full list")
        public void should_format_list() {
            output = formatter.listItems("First", "Second", "Third");
        }

        @Test
        @DisplayName("List with a title")
        public void should_format_list_with_title() {
            output = formatter.listItemsWithTitle("List title", "First", "Second", "Third");
        }

        /**
         * When no items, listItems method return an empty string.
         */
        @Test
        @DisplayName("Empty list")
        @TestOption(showRender = false)
        public void should_format_empty_list() {
            output = formatter.listItems();
        }
    }

    @Nested
    @DisplayName(value = "Block")
    class block {


        @Test
        @DisplayName("Warning")
        public void should_format_warning() {
            output = formatter.warning("Take care of that");
        }

        /**
         * A block id create an id in HTML file.
         * It can be used to define a style.
         */
        @Test
        @TestOption(extractAll = false)
        public void block_id() {
            output = formatter.blockId("myId");
        }

        /**
         * You can select block type from one of the Block enum.
         */
        @Test
        @DisplayName("Predefine blocks")
        @TestOption(extractAll = false)
        public void should_format_block_with_enum() {
            doc.write(formatter.listItemsWithTitle("Block value available",
                            Arrays.stream(Formatter.Block.values()).map(Enum::name).toArray(String[]::new)),
                    "");

            // >>>
            output = formatter.blockBuilder(Formatter.Block.LITERAL)
                    .title("Simple block")
                    .content("Into the block")
                    .build();
            // <<<
        }

        @Test
        @DisplayName("Free block")
        public void should_format_block() {
            output = formatter.blockBuilder("====")
                    .title("Simple block")
                    .content("Into the block")
                    .build();
        }

        /**
         * To display asciidoc in a block, it's necessary to escape some keyword
         * that are interpreted even in an uninterpreted block.
         *
         * With this option, all lines starting by `include::` add an anti-slash to escape this directive.
         */
        @Test
        @DisplayName("Escape asciidoc")
        @TestOption(normalOutput = true)
        public void escape_asciidoc(TestInfo testinfo) {

            // >>>
            output = formatter.blockBuilder("----")
                    .escapeSpecialKeywords()
                    .content("include::MyFile.txt[]")
                    .build();
            // <<<
            doc.write("",
                    "[red]##_Usage_##", "[source,java,indent=0]",
                    "----",
                    CodeExtractor.extractPartOfCurrentMethod(),
                    "----", "");

            doc.write("", "[red]##_Render_##", "", output, "");
            doc.write(":antislash: \\",
                    "[red]##_Asciidoc generated_##",
                    "[subs=attributes+]",  // add interpretation of attributes
                    "------",
                    output.replace("\\", "{antislash}"),  // replace \ by an attribute to be able to display the escaped value
                    "------", "");

            doc.write("", "___", "");
        }
    }

    @Nested
    class Source {

        @Test
        @DisplayName("Source code")
        public void should_format_source_code() {
            output = formatter.sourceCode(
                    "public int add(int a, int b) {\n" +
                            "   int result = a + b;\n" +
                            "   return result;\n" +
                            "}");
        }

        @Test
        @DisplayName("Source code using a builder")
        public void should_format_source_code_with_a_builder() {
            output = formatter.sourceCodeBuilder("groovy")
                    .title("Source code")
                    .indent(4)
                    .source(
                            "public int add(int a, int b) {\n" +
                                    "   int result = a + b;\n" +
                                    "   return result;\n" +
                                    "}")
                    .build()
            ;
        }

        @Test
        @DisplayName("Source code using a minimal builder")
        public void should_format_source_code_with_a_minimal_builder() {
            output = formatter.sourceCodeBuilder()
                    .source(
                            "public int add(int a, int b) {\n" +
                                    "   int result = a + b;\n" +
                                    "   return result;\n" +
                                    "}")
                    .build()
            ;
        }
    }

    @Nested
    class Include {
        @Test
        @DisplayName("Include another file")
        public void should_format_include() throws IOException {
            final String fileToInclude = "tmp/anotherFile.adoc";
            writeAFile(fileToInclude, "Text from another file included in this one");
            output = formatter.include(fileToInclude);
        }

        @Test
        @TestOption(showRender = false)
        public void add_a_leveloffset() throws IOException {
            final String fileToInclude = "tmp/anotherFile.adoc";
            writeAFile(fileToInclude, "Text from another file included in this one");
            output = formatter.include(fileToInclude, 2);
        }

        /**
         * Include directive always used '/' as directory separator regardless of operating system.
         * It allows to have same outputs when running tests in several environments.
         */
        @Test
        @TestOption(showRender = false)
        public void include_is_agnostic_of_directory_separator() throws IOException {
            output = String.join("\n",
                    formatter.include("tmp/anotherFile.adoc"),
                    "",
                    formatter.include("tmp\\anotherFile.adoc"));
        }

        @Test
        public void include_with_a_tag() throws IOException {

            final String fileToInclude = "tmp/fileWithTag.adoc";
            writeAFile(fileToInclude, String.join("\n",
                    "Text with tag",
                    "tag::myTag[]",
                    "text inside tag",
                    "end::myTag[]",
                    "text after tag")
            );
            output = formatter.include_with_tag(fileToInclude, "myTag");
        }

        @Test
        public void include_with_a_range_of_lines() throws IOException {

            final String fileToInclude = "tmp/fileWithRange.adoc";
            writeAFile(fileToInclude, String.join("\n",
                    "* line 1",
                    "* line 2",
                    "* line 3",
                    "* line 4",
                    "* line 5")
            );
            output = formatter.include_with_lines(fileToInclude, 2, 4);
        }
    }

    @Nested
    class Table {
        @Test
        @DisplayName("Display data")
        public void should_format_table() throws IOException {
            output = formatter.table(Arrays.asList(
                    Arrays.asList("A", "B", "C"),
                    Arrays.asList("x", "y", "z"),
                    Arrays.asList("1", "2", "3")
            ));
        }

        @Test
        @DisplayName("With an header")
        public void should_format_table_with_header() throws IOException {
            output = formatter.tableWithHeader(Arrays.asList(
                    Arrays.asList("A", "B", "C"),
                    Arrays.asList("x", "y", "z"),
                    Arrays.asList("1", "2", "3")
            ));
        }

        @Test
        @DisplayName("Header separate from data")
        public void should_format_table_with_header_separate_from_data() throws IOException {
            output = formatter.tableWithHeader(
                    Arrays.asList("A", "B", "C"),
                    Arrays.asList(
                            Arrays.asList("x", "y", "z"),
                            Arrays.asList("1", "2", "3")
                    ));
        }
    }
    @Test
    @DisplayName("Attribute")
    public void should_add_an_attribute() throws IOException {
        output = formatter.attribute("MY_ATTRIBUTE", "The value");
    }

    @Nested
    class Image {
        @Test
        public void should_add_an_image() {
            output = formatter.image("doc_as_test.png");
        }

        /**
         * With a title parameter, the text is shown when you mouse over the image.
         */
        @Test
        public void should_add_an_image_with_title() {
            output = formatter.image("doc_as_test.png", "doc as test");
        }
    }

    @AfterEach
    public void displaySource(TestInfo testinfo) {
        final Optional<TestOption> options = Optional.ofNullable(testinfo.getTestMethod()
                .get()
                .getAnnotation(TestOption.class));

        final TestOption annot = options.orElse(DEFAULT_OPTION);
        if (annot.normalOutput()) {
            return;
        }
        final String methodName = annot.includeMethodDoc();
        if (!methodName.isEmpty()) {
            try {
                final Optional<String> comment = CodeExtractor.getComment(AsciidocFormatter.class.getDeclaredMethod(methodName));
                if (comment.isPresent()) {
                    doc.write(comment.get(), "");
                }
                ;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        String methodCode = annot.extractAll()
                ? extractMethod(testinfo)
                : CodeExtractor.extractPartOfMethod(testinfo.getTestMethod().get());

        doc.write("",
                "[red]##_Usage_##",
                "[source,java,indent=0]",
                "----",
                methodCode,
                "----", "");

        if (options.map(TestOption::showRender).orElse(true)) {
            doc.write("", "[red]##_Render_##", "", output, "");
        }
        doc.write("\n[red]##_Asciidoc generated_##",
                formatter.blockBuilder("------")
                        .escapeSpecialKeywords()
                        .content(output)
                        .build(),
                "");

        doc.write("", "___", "");
    }

    public String extractMethod(TestInfo testinfo) {
        final String body = CodeExtractor.extractMethodBody(testinfo.getTestMethod().get());
        final String[] split = body.split("\n");
        final String bodyFormatted = Arrays.stream(Arrays.copyOfRange(split, 1, split.length - 1))
                .collect(Collectors.joining("\n"));
        return bodyFormatted;
    }

    private String extractMethod(Class classWithFormula, String methodName) {
        return CodeExtractor.extractMethodBody(classWithFormula, methodName);
    }

    public void writeAFile(String fileName, String text) throws IOException {

        final String canonicalName = this.getClass().getPackage().getName();
        final String pathName = canonicalName.toString().replace('.', '/');
        final Path filePath = doc.getDocPath().resolve(pathName).resolve(fileName);

        filePath.getParent().toFile().mkdirs();
        try (FileWriter writerInclude = new FileWriter(filePath.toFile())) {
            writerInclude.write(text);
        }
    }

}
