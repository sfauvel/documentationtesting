package org.sfvl.docformatter.asciidoc;

import org.sfvl.docformatter.*;
import org.sfvl.docformatter.Formatter;

import java.util.*;
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
     *
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
    public String paragraphSuite(String... paragraph) {
        return Arrays.stream(paragraph)
                .filter(Objects::nonNull)
                .filter(t -> !t.trim().isEmpty())
                .collect(Collectors.joining("\n\n"));
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
    public String listItemsWithTitle(String title, String... texts) {
        return String.format(".%s\n%s",
                title,
                listItems(texts).trim());
    }


    @Override
    public String sourceCode(String source) {
        return block("----", "source,java,indent=0", source);
    }

    @Override
    public String include(String filename) {
        return include_with_options(filename, "");
    }

    @Override
    public String include(String filename, int offset) {
        return include_with_options(filename, String.format("leveloffset=+%d", offset));
    }

    public String include_with_tag(String filename, String tag) {
        return include_with_options(filename, String.format("tag=%s", tag));
    }

    public String include_with_lines(String filename, int begin, int end) {
        return include_with_options(filename, String.format("lines=%d..%d", begin, end));
    }

    private String include_with_options(String filename, String options) {
        return String.format("include::%s[%s]", filename.replaceAll("\\\\", "/"), options);
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
    public String anchor(String id) {
        return "[[" + formatAnchorId(id) + "]]";
    }

    @Override
    public String linkToAnchor(String id, String visibleText) {
        return "<<" +
                formatAnchorId(id) +
                ((visibleText.isEmpty()) ? "" : "," + visibleText) +
                ">>";
    }

    @Override
    public String linkToPage(String address, String visibleText) {
        return linkToPage(address, null, visibleText);
    }

    @Override
    public String linkToPage(String address, String anchor, String visibleText) {
        return String.format("link:%s%s[%s]",
                address,
                (anchor == null) ? "" : "#" + formatAnchorId(anchor),
                visibleText);
    }

    @Override
    public String table(List<List<? extends Object>> data) {
        return "\n|====\n" +
                data.stream().map(this::formatTableLine).collect(Collectors.joining("\n")) +
                "\n|====\n";
    }

    @Override
    public String tableWithHeader(List<List<?>> data) {
        return tableWithHeader(data.get(0),
                data.subList(1, data.size()));
    }

    @Override
    public String tableWithHeader(List<?> header, List<List<?>> data) {
        return String.join("\n",
                "",
                "|====",
                formatTableLine(header),
                "",
                data.stream().map(this::formatTableLine).collect(Collectors.joining("\n")),
                "|====",
                "");
    }

    private String formatTableLine(List<?> line) {
        return line.stream().map(Object::toString).collect(Collectors.joining("|", "|", ""));
    }

    @Override
    public String image(String filename) {
        return String.format("\nimage::%s[]\n", filename);
    }

    @Override
    public String image(String filename, String title) {
        return String.format("\nimage:%s[title=\"%s\"]\n", filename, title);
    }

    @Override
    public String sourceFragment(String filename, String tag) {
        return "\n----\n"
                + String.format("include::{sourcedir}/%s[tags=%s]\n", filename, tag)
                + "----\n";
    }

    public Source source(String filename) {
        return new Source(filename);
    }

    private String block(String delimiter, String name, String message) {
        return String.format("\n[%s]\n%s\n%s\n%s\n", name, delimiter, message, delimiter);
    }

    private String formatAnchorId(String id) {
        return id.replaceAll("[\\.$\\: #]", "_").toLowerCase();
    }

    @Override
    public String blockId(String id) {
        return String.format("[#%s]", id);
    }

    @Override
    public BlockBuilder blockBuilder(String delimiter) {
        return new AsciidocBlockBuilder(delimiter);
    }

    private static Map<Block, String> delimiters = new HashMap() {{
        put(Block.LITERAL, "....");
        put(Block.CODE, "----");
    }};

    @Override
    public BlockBuilder blockBuilder(Block block) {
        return blockBuilder(delimiters.getOrDefault(block, ""));
    }

    @Override
    public SourceCodeBuilder sourceCodeBuilder() {
        return new AsciidocSourceCodeBuilder();
    }

    @Override
    public SourceCodeBuilder sourceCodeBuilder(String language) {
        return new AsciidocSourceCodeBuilder(language);
    }

    @Override
    public String attribute(String attribute, String value) {
        return String.format(":%s: %s", attribute, value);
    }

    @Override
    public String bold(String text) {
        return "*" + text + "*";
    }

    @Override
    public String italic(String text) {
        return "_" + text + "_";
    }

    public static class AsciidocBlockBuilder extends AsciidocGenericBlockBuilder<BlockBuilder>
            implements BlockBuilder {

        public AsciidocBlockBuilder(String delimiter) {
            super(BlockBuilder.class, delimiter);
        }

    }

    public static class AsciidocGenericBlockBuilder<T> implements GenericBlockBuilder<T> {
        private final T myself;
        protected final String delimiter;
        protected Optional<String> title = Optional.empty();
        protected String content = "";
        protected final Map<String, String> mapOptions = new LinkedHashMap<>();
        protected boolean escapeSpecialKeywords = false;

        public AsciidocGenericBlockBuilder(Class<T> selfType, String delimiter) {
            this.myself = selfType.cast(this);
            this.delimiter = delimiter;
        }

        @Override
        public T title(String title) {
            this.title = Optional.ofNullable(title);
            return myself;
        }

        @Override
        public T content(String content) {
            this.content = content;
            return myself;
        }

        @Override
        public T escapeSpecialKeywords() {
            this.escapeSpecialKeywords = true;
            return myself;
        }

        protected T withOption(String option) {
            return withOption(option, null);
        }

        protected T withOption(String option, String value) {
            mapOptions.put(option, value);
            return myself;
        }

        @Override
        public String build() {
            AsciidocFormatter formatter = new AsciidocFormatter();
            return String.format("%s%s%s",
                    title.map(t -> "." + t + "\n").orElse(""),
                    buildOptions().map(opt -> opt + "\n").orElse(""),
                    String.format("%s\n%s\n%s", delimiter, formatContent(), delimiter));
        }

        private String formatContent() {
            return escapeSpecialKeywords
                    ? content.replaceAll("(^|\\n)include", "$1\\\\include")
                    : content;
        }

        private Optional<String> buildOptions() {
            if (mapOptions.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(mapOptions.entrySet().stream()
                    .map(e -> e.getKey() + Optional.ofNullable(e.getValue()).map(v -> "=" + v).orElse(""))
                    .collect(Collectors.joining(",", "[", "]")));
        }
    }

    public static class AsciidocSourceCodeBuilder extends AsciidocGenericBlockBuilder<SourceCodeBuilder>
            implements SourceCodeBuilder {

        public AsciidocSourceCodeBuilder() {
            this(null);
        }

        public AsciidocSourceCodeBuilder(String language) {
            super(SourceCodeBuilder.class, "----");
            withOption("source" + Optional.ofNullable(language).map(t -> "," + t).orElse(""));
            withOption("indent", "0");
        }

        @Override
        public SourceCodeBuilder indent(int indent) {
            return withOption("indent", Integer.toString(indent));
        }

        @Override
        public SourceCodeBuilder source(String source) {
            return content(source);
        }

    }
}

