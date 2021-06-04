package org.sfvl.demo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
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
import java.util.Arrays;
import java.util.stream.Collectors;

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
                ":nofooter:",
                "include::" + docPath.approved().from(Config.DOC_PATH).toString() + "[]");

        final Path indexFile = Config.DOC_PATH.resolve("Documentation.adoc");
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
    void show_hide_part_of_document() throws IOException {
        final DocPath docPath = new DocPath(Paths.get(""), "buttonShowHide");
        final Path from = docPath.resource().from(this.getClass());

        doc.write(formatter.include(from.toString()));
        doc.write(
                String.format(".Asciidoc source used"),
                "------",
                getEscapedFileContent(docPath.resource().path()),
                "------"
        );
    }

    private String adocFileSourceEscaped(Path file) throws IOException {
        return String.join("\n",
                ".Document generated",
                "------",
                getEscapedFileContent(file),
                "------");
    }

    private String getEscapedFileContent(Path file) throws IOException {
        final String content = Files.lines(file)
                .collect(Collectors.joining("\n"));
        return escapedAdocSpecialKeywords(content);
    }

    private String escapedAdocSpecialKeywords(String collect) {
        return collect
                .replaceAll("(^|\\n)include::", "$1\\\\include::")
                .replaceAll("(^|\\n)ifndef::", "$1\\\\ifndef::")
                .replaceAll("(^|\\n)ifdef::", "$1\\\\ifdef::")
                .replaceAll("(^|\\n)endif::", "$1\\\\endif::");
    }

}

