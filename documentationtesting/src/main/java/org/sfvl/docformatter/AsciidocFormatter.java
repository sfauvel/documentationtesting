package org.sfvl.docformatter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AsciidocFormatter implements Formatter {

    @Override
    public String standardOptions() {
        return String.join("\n",
                ":sourcedir: ..",
                ":source-highlighter: rouge",
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
    public Formatter.Source source(String filename) {
        return new Formatter.Source(filename);
    }

    private String block(String delimiter, String name, String message) {
        return String.format("\n[%s]\n%s\n%s\n%s\n", name, delimiter, message, delimiter);
    }

    private String formatLink(String id) {
        return id.replaceAll("[\\.$\\: #]", "_").toLowerCase();
    }
}

