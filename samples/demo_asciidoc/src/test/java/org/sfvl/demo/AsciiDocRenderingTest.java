package org.sfvl.demo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.Formatter;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.NoTitle;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Present how to render some common needs with asciidoc.
 */
public class AsciiDocRenderingTest {

    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    static private Formatter formatter = new AsciidocFormatter();

    @AfterAll
    static public void writeIndexPage() throws IOException, NoSuchMethodException {
        final DocPath docPath = new DocPath(AsciiDocRenderingTest.class);
        String content = String.join("\n",
                doc.getDocWriter().defineDocPath(Paths.get(".")),
                ":toc: left",
                ":nofooter:",
                "include::" + docPath.approved().from(Config.DOC_PATH).toString() + "[]");

        final Path indexFile = Config.DOC_PATH.resolve("index.adoc");
        try (FileWriter fileWriter = new FileWriter(indexFile.toFile())) {
            fileWriter.write(content);
        }
    }

    /**
     * You can display utf-8 characters with syntax: `\&#x2714;` to display `&#x2714;`.
     *
     * See: https://www.w3schools.com/charsets/ref_html_utf8.asp
     */
    @Test
    void useful_symbol() {
        doc.write(symbols("2714"),
                "",
                symbols(
                        "2191", "2193",
                        "21A5", "21A7",
                        "21D1", "21D3",
                        "21E1", "21E3",
                        "21E7", "21E9"),
                "",
                symbols(
                        "2206", "2207",
                        "22C0", "22C1"),
                ""
        );
    }

    private String symbols(String... codes) {
        final String codeLine = Arrays.stream(codes)
                .map(code -> String.format("| `\\&#x%s;`", code))
                .collect(Collectors.joining("", "^", ""));

        final String renderLine = Arrays.stream(codes)
                .map(code -> String.format("| &#x%s;", code))
                .collect(Collectors.joining("", "^", ""));

        return String.join("\n",
                "[%autowidth]",
                "|====",
                codeLine,
                renderLine,
                "|====");
    }

    @Test
    @NoTitle
    void show_hide_part_of_document() {
        final DocPath docPath = new DocPath(Paths.get(""), "buttonShowHide");
        final Path from = docPath.resource().from(this.getClass());

        doc.write(formatter.include(from.toString(), 1),
                "",
                adocFileSourceEscaped(docPath.resource().path(), "Asciidoc source used")
        );
    }

    /**
     * To include something once, like css, javascript or a special page, we can use attribute and endif directive.
     *
     * If the attribute is not defined (ifndef), we defined it and do what it have to do once (include a css, ...).
     * The second time, we not passe in the ifndef part.
     */
    @Test
    void include_once() {
        final DocPath docPath = new DocPath(Paths.get(""), "includeOnce");
        final Path from = docPath.resource().from(this.getClass());

        final String ifndefDirectives = String.join("\n",
                ifndef("MY_ATTRIBUTE", "First value", "First declaration"),
                "",
                ifndef("MY_ATTRIBUTE", "Second value", "Second declaration"),
                "",
                "MY_ATTRIBUTE value: *{MY_ATTRIBUTE}*"
        );

        final String compactIfndefDirectives = String.join("\n",
                "ifndef::COMPACT_SYNTAX[:COMPACT_SYNTAX: My value]",
                "",
                "COMPACT_SYNTAX value: *{COMPACT_SYNTAX}*",
                "");

        doc.write(".Code we add in asciidoc file",
                "------",
                escapedAdocSpecialKeywords(ifndefDirectives),
                "------",
                "",
                "We can see below the result of the inclusion of those directives.",
                "We only see `First declaration` text from the inside of first ifndef",
                "and MY_ATTRIBUTE value is the default value of the first ifndef.",
                "",
                ifndefDirectives,
                "",
                "If we just want to define a default, you can use a more compact syntax.",
                "",
                ".Code we add in asciidoc file",
                "------",
                escapedAdocSpecialKeywords(compactIfndefDirectives),
                "------",
                "Below, what we can see on the document.",
                "",
                compactIfndefDirectives

        );

    }

    @Nested
    class DrawWithATable {

        @Test
        public void how_to_do() {
            String table_code = String.join("\n",
                    "[.tableStyled]",
                    "[%autowidth, cols=2*a]",
                    "|====",
                    "|",
                    "&nbsp;",
                    "| [.yellow]",
                    "&nbsp;",
                    "|",
                    "&nbsp;",
                    "|",
                    "&nbsp;",
                    "|====");
            String style = String.join("\n",
                    "<style>",
                    "/* To fill the cell with background */",
                    ".tableStyled td {",
                    "    padding: 0;",
                    "}",
                    "/* To make a square */",
                    ".tableStyled p {",
                    "    width: 2em;",
                    "    line-height: 2em;",
                    "}",
                    ".tableStyled .yellow {",
                    "    background-color:yellow;",
                    "    color:yellow;",
                    "}",
                    "",
                    "</style>",
                    "");

            doc.write("Cells must be interpreted as asciidoc using 'a'.",
                    "Here, we set all cells using `cols` attribute on table.",
                    "",
                    "To set a style in a cell, you can add style using `[.my_style]` at the beginning of the cell.",
                    "It must be before the content with style to apply and must not have any content after it on the same line.",
                    "",
                    "We need to put a text in cell.",
                    "We prefer `&nbsp;` to avoir some display problems when cell is too small.",
                    "",
                    table_code,
                    "",
                    "",
                    ".Click to see asciidoc code",
                    "[%collapsible]",
                    "====",
                    "[,asciidoc]",
                    "----", table_code,
                    "----",
                    "====",
                    "",
                    "",
                    ".Click to see css code",
                    "[%collapsible]",
                    "====",
                    "[,css]",
                    "----", style,
                    "----",
                    "====",
                    "",
                    "");

            doc.write("++++",
                    style,
                    "++++");

        }

        @Test
        public void draw_a_smiley() {
            final String line = IntStream.range(0, 15).mapToObj(__ -> "| &nbsp; ").collect(Collectors.joining());
            final String table = IntStream.range(0, 15).mapToObj(__ -> line).collect(Collectors.joining("\n"));
            String table_code = String.join("\n",
                    "[.tableStyled.smiley]",
                    "[%autowidth, cols=15*a]",
                    "|====",
                    table,
                    "|====");

            String style = String.join("\n",
                    "<style>",
                    "/* To fill the cell with background */",
                    ".tableStyled.smiley td {",
                    "    padding: 0;",
                    "}",
                    ".tableStyled.smiley p {",
                    "    width: 5px;",
                    "    line-height: 5px;",
                    "}",
                    "",
                    ".tableStyled.smiley td {",
                    "    background-color:white;",
                    "    color:white;",
                    "    border: none;",
                    "}",
                    "",
                    ".tableStyled.smiley tr:nth-child(1) td:nth-child(n+6):nth-child(-n+10),",
                    ".tableStyled.smiley tr:nth-child(2) td:nth-child(n+4):nth-child(-n+12),",
                    ".tableStyled.smiley tr:nth-child(3) td:nth-child(n+3):nth-child(-n+13),",
                    ".tableStyled.smiley tr:nth-child(4) td:nth-child(n+2):nth-child(-n+14),",
                    ".tableStyled.smiley tr:nth-child(5) td:nth-child(n+2):nth-child(-n+14),",
                    ".tableStyled.smiley tr:nth-child(6) td,",
                    ".tableStyled.smiley tr:nth-child(7) td:nth-child(n+1):nth-child(-n+4),",
                    ".tableStyled.smiley tr:nth-child(7) td:nth-child(n+7):nth-child(-n+10),",
                    ".tableStyled.smiley tr:nth-child(7) td:nth-child(n+13):nth-child(-n+15),",
                    ".tableStyled.smiley tr:nth-child(8) td:nth-child(n+1):nth-child(-n+5),",
                    ".tableStyled.smiley tr:nth-child(8) td:nth-child(n+7):nth-child(-n+11),",
                    ".tableStyled.smiley tr:nth-child(8) td:nth-child(n+13):nth-child(-n+15),",
                    ".tableStyled.smiley tr:nth-child(9) td:nth-child(n+1):nth-child(-n+15),",
                    ".tableStyled.smiley tr:nth-child(10) td:nth-child(n+1):nth-child(-n+15),",
                    ".tableStyled.smiley tr:nth-child(11) td:nth-child(n+2):nth-child(-n+14),",
                    ".tableStyled.smiley tr:nth-child(12) td:nth-child(n+2):nth-child(-n+14),",
                    ".tableStyled.smiley tr:nth-child(13) td:nth-child(n+3):nth-child(-n+13),",
                    ".tableStyled.smiley tr:nth-child(14) td:nth-child(n+4):nth-child(-n+5),",
                    ".tableStyled.smiley tr:nth-child(14) td:nth-child(n+11):nth-child(-n+12),",
                    ".tableStyled.smiley tr:nth-child(15) td:nth-child(n+6):nth-child(-n+10)",
                    "{",
                    "    background-color:black;",
                    "    color:black;",
                    "}",
                    ".tableStyled.smiley tr:nth-child(2) td:nth-child(n+6):nth-child(-n+10),",
                    ".tableStyled.smiley tr:nth-child(3) td:nth-child(n+4):nth-child(-n+12),",
                    ".tableStyled.smiley tr:nth-child(4) td:nth-child(n+3):nth-child(-n+13),",
                    ".tableStyled.smiley tr:nth-child(5) td:nth-child(n+3):nth-child(-n+13),",
                    ".tableStyled.smiley tr:nth-child(7) td:nth-child(n+2):nth-child(-n+2),",
                    ".tableStyled.smiley tr:nth-child(7) td:nth-child(n+14):nth-child(-n+14),",
                    ".tableStyled.smiley tr:nth-child(8) td:nth-child(n+2):nth-child(-n+2),",
                    ".tableStyled.smiley tr:nth-child(8) td:nth-child(n+8):nth-child(-n+8),",
                    ".tableStyled.smiley tr:nth-child(8) td:nth-child(n+14):nth-child(-n+14),",
                    ".tableStyled.smiley tr:nth-child(9) td:nth-child(n+2):nth-child(-n+3),",
                    ".tableStyled.smiley tr:nth-child(9) td:nth-child(n+7):nth-child(-n+9),",
                    ".tableStyled.smiley tr:nth-child(9) td:nth-child(n+13):nth-child(-n+14),",
                    ".tableStyled.smiley tr:nth-child(10) td:nth-child(n+2):nth-child(-n+14),",
                    ".tableStyled.smiley tr:nth-child(11) td:nth-child(n+3):nth-child(-n+4),",
                    ".tableStyled.smiley tr:nth-child(11) td:nth-child(n+6):nth-child(-n+13),",
                    ".tableStyled.smiley tr:nth-child(12) td:nth-child(n+3):nth-child(-n+5),",
                    ".tableStyled.smiley tr:nth-child(12) td:nth-child(n+10):nth-child(-n+13),",
                    ".tableStyled.smiley tr:nth-child(13) td:nth-child(n+4):nth-child(-n+12),",
                    ".tableStyled.smiley tr:nth-child(14) td:nth-child(n+6):nth-child(-n+10)",
                    "{",
                    "    background-color:yellow;",
                    "    color:yellow;",
                    "}",
                    "</style>",
                    "");

            doc.write("We can play a little and draw a smiley for example.",
                    "It's not very useful but we want to show how we can draw something using a table.",
                    "",
                    table_code,
                    "",
                    "",
                    ".Click to see asciidoc code",
                    "[%collapsible]",
                    "====",
                    "[,asciidoc]",
                    "----", table_code,
                    "----",
                    "====",
                    "",
                    "",
                    ".Click to see css code",
                    "[%collapsible]",
                    "====",
                    "[,css]",
                    "----", style,
                    "----",
                    "====",
                    "",
                    "");

            doc.write("++++",
                    style,
                    "++++");

        }

        public String rect(int x, int y, int height, int width, String fill, String stroke, int strokeWidth) {
            return String.format("<rect x=\"%d\" y=\"%d\" height=\"%d\" width=\"%d\" fill=\"%s\" stroke=\"%s\" stroke-width=\"%d\" />",
                    x, y, height, width, fill, stroke, strokeWidth);
        }

        public String cell(int x, int y, String fill, int square_size) {
            return String.format("<use xlink:href=\"#%s_square\" x=\"%d\" y=\"%d\"/>", fill, x * square_size, y * square_size);
            //  return rect(x * square_size, y * square_size, square_size, square_size, fill, "grey", 1);
        }

        public String table(int width, int height, int square_size) {
            List<String> table = new ArrayList<>();
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    table.add(cell(i, j, "white", square_size));
                }
            }
            return table.stream().collect(Collectors.joining("\n"));
        }

        @Test
        public void using_svg() {
            final int width = 3;
            final int height = 3;
            final int square_size = 30;

            String table_code = String.join("\n",
                    String.format("<svg version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"%d\" height=\"%d\" >",
                            width * square_size + 1, width * square_size + 1),
                    "<g id=\"square\">",
                    "    <rect height=\"30\" width=\"30\" stroke=\"grey\" stroke-width=\"1\">",
                    "</g>",
                    "<g id=\"blue_square\">",
                    "    <use xlink:href=\"#square\" fill=\"blue\"/>",
                    "</g>",
                    "<g id=\"white_square\">",
                    "    <use xlink:href=\"#square\" fill=\"white\"/>",
                    "</g>",
                    table(width, height, square_size),
                    "",
                    cell(1, 1, "blue", square_size),
                    "</svg>");

            doc.write(
                    "We can create a svg included in asciidoc file to draw a table.",
                    "It gives a lot of possibilities to draw what we need",
                    "but, it's a bit difficult to use the svg to code to understand difference wit the reference file.",
                    "Moreover, the creation of the svg is not easy.",
                    "++++",
                    table_code,
                    "++++",
                    "&nbsp;", // Need to add a space to make collapsible work
                    "",
                    ".Click to see asciidoc code",
                    "[%collapsible]",
                    "====",
                    "[,asciidoc]",
                    "----",
                    table_code,
                    "----",
                    "====",
                    "",
                    "");
        }
    }

    private String ifndef(String attribute, String defaultValue, String other) {
        return String.format("ifndef::%s[]\n:%s: %s\n%s\nendif::[]", attribute, attribute, defaultValue, other);
    }

    private String adocFileSourceEscaped(Path file, String title) {
        return String.join("\n",
                "." + title,
                "------",
                getEscapedFileContent(file),
                "------");
    }

    private String getEscapedFileContent(Path file) {
        try {
            final String content = Files.lines(file)
                    .collect(Collectors.joining("\n"));
            return escapedAdocSpecialKeywords(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String escapedAdocSpecialKeywords(String collect) {
        return collect
                .replaceAll("(^|\\n)include::", "$1\\\\include::")
                .replaceAll("(^|\\n)ifndef::", "$1\\\\ifndef::")
                .replaceAll("(^|\\n)ifdef::", "$1\\\\ifdef::")
                .replaceAll("(^|\\n)endif::", "$1\\\\endif::");
    }

}

