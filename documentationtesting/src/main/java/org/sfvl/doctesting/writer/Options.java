package org.sfvl.doctesting.writer;

import org.sfvl.docformatter.Formatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Options {
    private final Formatter formatter;

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

    public String withCode() {
        return getDocumentOptions(Stream.of(
                new Option("toc", "left"),
                new Option("nofooter"),
                new Option("stem"),
                new Option("source-highlighter", "rouge"),
                new Option("toclevels", "4")
        ));
    }
}
