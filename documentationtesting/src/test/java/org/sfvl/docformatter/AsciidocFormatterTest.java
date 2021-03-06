package org.sfvl.docformatter;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.utils.CodeExtractor;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.ClassToDocument;

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
public class AsciidocFormatterTest {
    private static AsciidocFormatter formatter = new AsciidocFormatter();
    private String output;

    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestOption {
        /// False if 'render' section should not be added to the final document.
        boolean showRender() default true;

        /// The name of the method in AsciidocFormatter that used to get documentation.
        String includeMethodDoc() default "";
    }

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

    @Test
    @DisplayName("Warning")
    public void should_format_warning() {
        output = formatter.warning("Take care of that");
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

    @Test
    @DisplayName("Anchor")
    public void should_format_anchorLink() {
        output = formatter.anchorLink("AnchorExample", "This is a link to anoter place");
    }

    @Test
    @DisplayName("Link")
    public void should_format_link() {
        output = formatter.link("AnchorExample") + "You can make an anchor to here";
    }

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

    /**
     * When no items, listItems method return an empty string.
     */
    @Test
    @DisplayName("Empty list")
    @TestOption(showRender = false)
    public void should_format_empty_list() {
        output = formatter.listItems();
    }

    @Test
    @DisplayName("Source code")
    public void should_format_source_code() {
        output = formatter.sourceCode(
                "public int add(int a, int b) {\n" +
                        "   int result = a + b;\n" +
                        "   return result;\n" +
                        "}");
    }

    @Nested
    class block {
        /**
         * You can select block type from on of the Block enum.
         */
        @Test
        @DisplayName("Predefine blocks")
        public void should_format_block_with_enum() {
            output = formatter.blockBuilder(Formatter.Block.LITERAL)
                    .title("Simple block")
                    .content("Into the block")
                    .build();
        }

        @Test
        @DisplayName("Free block")
        public void should_format_block() {
            output = formatter.blockBuilder("====")
                    .title("Simple block")
                    .content("Into the block")
                    .build();
        }
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

    @Test
    @DisplayName("Include another file")
    public void should_format_include() throws IOException {
        final String fileToInclude = "tmp/anotherFile.adoc";
        writeAFile(fileToInclude, "Text from another file included in this one");
        output = formatter.include(fileToInclude);
    }

    @Test
    @DisplayName("Table")
    public void should_format_table() throws IOException {
        output = formatter.table(Arrays.asList(
                Arrays.asList("A", "B", "C"),
                Arrays.asList("x", "y", "z"),
                Arrays.asList("1", "2", "3")
        ));
    }

    @Test
    @DisplayName("Table with header")
    public void should_format_table_with_header() throws IOException {
        output = formatter.tableWithHeader(Arrays.asList(
                Arrays.asList("A", "B", "C"),
                Arrays.asList("x", "y", "z"),
                Arrays.asList("1", "2", "3")
        ));
    }

    @AfterEach
    public void displaySource(TestInfo testinfo) {

        final Optional<TestOption> annotation = Optional.ofNullable(testinfo.getTestMethod()
                .get()
                .getAnnotation(TestOption.class));

        annotation.map(TestOption::includeMethodDoc)
                .filter(methodName -> !methodName.isEmpty())
                .map(methodName -> CodeExtractor.getComment(AsciidocFormatter.class, methodName))
                .ifPresent(comment -> doc.write(comment.get(), ""));

        doc.write("", "[red]##_Usage_##", "[source,java,indent=0]", "----", extractMethod(testinfo), "----", "");

        if (annotation.map(TestOption::showRender).orElse(true)) {
            doc.write("", "[red]##_Render_##", "", output, "");
        }
        doc.write("\n[red]##_Asciidoc generated_##", "------", output.replaceAll("\\ninclude", "\n\\\\include"), "------", "");

        doc.write("", "___", "");
    }

    public String extractMethod(TestInfo testinfo) {
        final String body = extractMethod(this.getClass(), testinfo.getTestMethod().get().getName());
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
