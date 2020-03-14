package fr.sfvl.documentationtesting;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

public interface Formatter {

    String standardOptions();

    String title(int index, String title);

    String description(String description);

    String paragraph(String... content);

    String tableOfContent();

    String addDefinition(String key, String description);

    String listItem(String text);

    String listItems(String... texts);

    String sourceCode(String source);

    String startDocument(String title);

    String include(String filename);

    String warning(String message);

    String section(String name, String message);

    String link(String id);

    String anchorLink(String id, String visibleText);

    String table(List<List<? extends Object>> data);

    String image(String filename);

    String sourceFragment(String s, String interestingCode);

    Source source(String s);


    public static class Source {
        private String filename;
        private String tag;
        private String language;
        private String legend;

        public Source(String filename) {
            this.filename = filename;
        }

        @Override
        public String toString() {
            return Arrays.asList(
                    "",
                    languageToString(),
                    legendToString(),
                    "----",
                    String.format("include::{sourcedir}/%s%s",
                            filename,
                            ofNullable(tagToString()).orElse("")),
                    "----",
                    "")
                    .stream()
                    .filter(obj -> !Objects.isNull(obj))
                    .collect(Collectors.joining("\n"));

        }

        private String legendToString() {
            return legend == null
                    ? null
                    : String.format(".%s", legend);
        }

        private String languageToString() {
            return language == null
                    ? null
                    : String.format("[source,%s,indent=0]", language);
        }


        private String tagToString() {
            return tag == null
                    ? null
                    : String.format("[tags=example]", tag);
        }

        public Source withTag(String tag) {
            this.tag = tag;
            return this;
        }

        public Source withLanguage(String language) {
            this.language = language;
            return this;

        }

        public Source withLegend(String legend) {
            this.legend = legend;
            return this;

        }
    }

    public static class AsciidoctorFormatter implements Formatter {

        @Override
        public String standardOptions() {
            return String.join("\n",
                    ":sourcedir: ..",
                    ":source-highlighter: coderay",
                    ":docinfo:",
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
            return ":toc: left\n:toclevels: 4\n";
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

            return texts.length == 0
                    ? ""
                    : Arrays.stream(texts).collect(Collectors.joining("\n"));
        }

        @Override
        public String sourceCode(String source) {
            return block("----", "source,java,indent=0", source);
        }

        @Override
        public String startDocument(String title) {
            return String.format("= %s\n:toc: left\n:toclevels: 3\n:sectlinks:\n:source-highlighter: coderay", title);
        }

        @Override
        public String include(String filename) {
            return String.format("\ninclude::%s[leveloffset=+1]\n", filename);
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
                    data.stream().map(line -> {
                        return line.stream().map(Object::toString).collect(Collectors.joining("|", "|", "\n"));
                    }).collect(Collectors.joining()) +
                    "|====\n";
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
    }

}
