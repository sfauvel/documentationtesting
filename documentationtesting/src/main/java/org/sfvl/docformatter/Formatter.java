package org.sfvl.docformatter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

public interface Formatter {

    String standardOptions();

    String title(int index, String title);

    String description(String description);

    String paragraph(String... content);

    String tableOfContent();

    String tableOfContent(int level);

    String addDefinition(String key, String description);

    String listItem(String text);

    String listItems(String... texts);

    String sourceCode(String source);

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

}
