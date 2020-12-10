package org.sfvl.docformatter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * `AsciidocFormatter` provides high level methods to generate asciidoc text.
 */
public class AsciidocFormatter implements Formatter {
    /**
     * Standard option to add at the begining of the document.
     * (link:https://asciidoctor.org/docs/user-manual/#builtin-attributes[Description from asciidoctor Documentation])
     *
     * - *sourcedir*: Path to locate source files.
     * - *source-highlighter*: Source code highlighter to use.
     * - *docinfo*:
     * @returnlink:https://asciidoctor.org/docs/user-manual/#docinfo-file[Add custom header and footer]
     */
    @Override
    public String standardOptions() {
        return String.join("\n",
                ":sourcedir: ..",
                ":source-highlighter: rouge",
                ":docinfo:",
//                ":sectlinks:" ,
                ""
        );
    }

    @Override
    public String title(int index, String title) {
        return String.format(
                "\n%s %s\n",
                new String(new char[index]).replace('\0', '='),
                title);
    }

    @Override
    public String description(String description) {
        return description + "\n";
    }

    @Override
    public String paragraph(String... content) {
        return Arrays.stream(content).collect(Collectors.joining("\n")) + "\n\n";
    }

    @Override
    public String tableOfContent() {
        return tableOfContent(4);
    }

    @Override
    public String tableOfContent(int level) {
        return String.format(":toc: left\n:toclevels: %d, \n", level);
    }

    @Override
    public String addDefinition(String key, String description) {
        return String.format("\n%s:: %s\n", key, (description.isEmpty() ? "\n+" : description));
    }


    @Override
    public String listItem(String text) {
        return "\n* " + text;
    }

    @Override
    public String listItems(String... texts) {
        return Arrays.stream(texts)
                    .map(this::listItem)
                    .collect(Collectors.joining());
    }

    @Override
    public String sourceCode(String source) {
        return block("----", "source,java,indent=0", source);
    }

    @Override
    public String include(String filename) {
        return include(filename, 1);
    }

    @Override
    public String include(String filename, int offset) {
        return String.format("\ninclude::%s[leveloffset=+%d]\n", filename, offset);
    }

    @Override
    public String warning(String message) {
        return block("====", "WARNING", message);
    }

    @Override
    public String section(String name, String message) {
        return block("--", name, message);
    }

    @Override
    public String link(String id) {
        return "[[" + formatLink(id) + "]]";
    }

    @Override
    public String anchorLink(String id, String visibleText) {
        return "<<" +
                formatLink(id) +
                ((visibleText.isEmpty()) ? "" : "," + visibleText) +
                ">>";
    }

    @Override
    public String table(List<List<? extends Object>> data) {
        return "\n|====\n" +
                data.stream().map(this::formatTableLine).collect(Collectors.joining("\n")) +
                "\n|====\n";
    }

    @Override
    public String tableWithHeader(List<List<?>> data) {
        return "\n|====\n" +
                data.stream().limit(1).map(this::formatTableLine).collect(Collectors.joining("\n")) +
                "\n\n"+
                data.stream().skip(1).map(this::formatTableLine).collect(Collectors.joining("\n")) +
                "\n|====\n";
    }

    private String formatTableLine(List<?> line) {
        return line.stream().map(Object::toString).collect(Collectors.joining("|", "|", ""));
    }

    @Override
    public String image(String filename) {
        return String.format("\nimage::%s[]\n", filename);
    }

    @Override
    public String sourceFragment(String filename, String tag) {
        return "\n----\n"
                + String.format("include::{sourcedir}/%s[tags=%s]\n", filename, tag)
                + "----\n";
    }

    @Override
    public Source source(String filename) {
        return new Source(filename);
    }

    private String block(String delimiter, String name, String message) {
        return String.format("\n[%s]\n%s\n%s\n%s\n", name, delimiter, message, delimiter);
    }

    private String formatLink(String id) {
        return id.replaceAll("[\\.$\\: #]", "_").toLowerCase();
    }

    @Override
    public SourceCodeBuilder sourceCodeBuilder() {
        return new AsciidocSourceCodeBuilder();
    }

    public  static class AsciidocSourceCodeBuilder implements SourceCodeBuilder {
        private String title;
        private String language;
        private int indent;
        private String source;

        @Override
        public SourceCodeBuilder title(String title) {
            this.title = title;
            return this;
        }

        @Override
        public SourceCodeBuilder language(String language) {
            this.language = language;
            return this;
        }

        @Override
        public SourceCodeBuilder indent(int indent) {
            this.indent = indent;
            return this;
        }

        @Override
        public SourceCodeBuilder source(String source) {
            this.source = source;
            return this;
        }

        @Override
        public String build() {
            AsciidocFormatter formatter = new AsciidocFormatter();

            return String.format(".%s%s",
                    title,
                    formatter.block("----", String.format("source,%s,indent=%d", language, indent), source));
        }
    }
}

