package org.sfvl.doctesting.writer;

import org.sfvl.docformatter.Formatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Options {
    private final Formatter formatter;
    private final List<Option> options = new ArrayList<>();

    public Options(Formatter formatter) {
        this.formatter = formatter;
    }

    private String getDocumentOptions(Option... options) {
        return getDocumentOptions(Arrays.asList(options));
    }

    private String getDocumentOptions(List<Option> options) {
        return getDocumentOptions(options.stream());
    }

    private String getDocumentOptions(Stream<Option> stream) {
        return formatter.paragraph(stream
                .map(Option::format)
                .collect(Collectors.toList()).toArray(new String[0]));
    }

    public String build() {
        return getDocumentOptions(options);
    }

    public String withCode() {
        with("toc", "left");
        with("nofooter");
        with("stem");
        with("source-highlighter", "rouge");
        return build();
    }

    public org.sfvl.doctesting.writer.Options with(String key, String value) {
        options.add(new Option(key, value));
        return this;
    }

    public org.sfvl.doctesting.writer.Options with(String key) {
        options.add(new Option(key));
        return this;
    }

    public org.sfvl.doctesting.writer.Options remove(String key) {
        options.removeIf(o -> o.key.equals(key));
        return this;
    }
}
