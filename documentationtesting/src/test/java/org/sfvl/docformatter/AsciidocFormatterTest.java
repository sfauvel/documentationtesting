package org.sfvl.docformatter;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.SourceRoot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.sfvl.doctesting.ApprovalsBase;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * `AsciidocFormatter` provides high level methods to generate asciidoc text.
 *
 * Each section of this documentation explain one method of the `AsciidocFormatter`.
 * For each method, we describe:
 *
 * - how it can be uses in code
 * - what the asciidoc generated will look like at the end (except when it's not possible)
 * - the asciidoc text generated
 */
@DisplayName("Asciidoctor formatter")
public class AsciidocFormatterTest extends ApprovalsBase {
    private static AsciidocFormatter formatter = new AsciidocFormatter();
    private String output;

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

    @Test
    @DisplayName("Include another file")
    public void should_format_include() throws IOException {
        final String fileToInclude = "tmp/anotherFile.adoc";
        writeAFile(fileToInclude, "Text from another file included in this one");
        output = formatter.include(fileToInclude);
    }

    @AfterEach
    public void displaySource(TestInfo testinfo) {

        final Optional<TestOption> annotation = Optional.ofNullable(testinfo.getTestMethod()
                .get()
                .getAnnotation(TestOption.class));

        annotation.map(TestOption::includeMethodDoc)
                .filter(name -> !name.isEmpty())
                .map(methodName -> getComment(AsciidocFormatter.class, methodName))
                .ifPresent(doc -> write(doc + "\n"));

        write("\n[red]##_Usage_##\n[source,java,indent=0]\n----\n");
        write(extractMethodBody(testinfo));

        write("\n----\n");

        if (annotation.map(TestOption::showRender).orElse(true)) {
            write("\n[red]##_Render_##\n\n");
            write(output);
            write("\n");
        }
        write("\n[red]##_Asciidoc generated_##\n------\n");
        write(output.replaceAll("\\ninclude", "\n\\\\include"));
        write("\n------\n");

        write("\n___\n");
    }

    public String extractMethodBody(TestInfo testinfo) {
        final String body = extractMethodBody(this.getClass(), testinfo.getTestMethod().get().getName());
        final String[] split = body.split("\n");
        final String bodyFormatted = Arrays.stream(Arrays.copyOfRange(split, 1, split.length - 1))
                .collect(Collectors.joining("\n"));
        return bodyFormatted;
    }

    private String extractMethodBody(Class classWithFormula, String methodName) {
        SourceRoot sourceRoot = new SourceRoot(Paths.get("src/test/java"));

        CompilationUnit cu = sourceRoot.parse(
                classWithFormula.getPackage().getName(),
                classWithFormula.getSimpleName() + ".java");

        StringBuffer javaCode = new StringBuffer();
        cu.accept(new VoidVisitorAdapter<StringBuffer>() {
            @Override
            public void visit(MethodDeclaration n, StringBuffer arg) {
                if (methodName.equals(n.getNameAsString())) {
                    final String str = n.getBody()
                            .map(body -> body.toString())
                            .orElse("");
                    javaCode.append(str);

                }
            }
        }, null);
        return javaCode.toString();
    }

    public void writeAFile(String fileName, String text) throws IOException {

        final String canonicalName = this.getClass().getPackage().getName();
        final String pathName = canonicalName.toString().replace('.', '/');
        final Path filePath = getDocPath().resolve(pathName).resolve(fileName);

        filePath.getParent().toFile().mkdirs();
        try (FileWriter writerInclude = new FileWriter(filePath.toFile())) {
            writerInclude.write(text);
        }
    }
}