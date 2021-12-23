package org.sfvl.howto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GraphvizGenerator {
    private Optional<RankDir> direction = Optional.empty();

    static public class Link {

        private final String from;
        private final String to;

        public Link(String from, String to) {
            this.from = from;
            this.to = to;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }
    }

    public enum RankDir {
        LeftRight("LR"), TopDown ("TD");

        private String code;

        RankDir(String code) {
            this.code = code;
        }
    }

    List<Link> links = new ArrayList<>();

    public String generate() {
        return generate("", "");
    }

    public String generate(String toInsertBefore, String toInsertAfter) {
        String tab = "    ";
        return "\n[graphviz]\n" +
                "----\n" +
                "digraph g {\n" +
                tabulateText(tab,
                        direction.map(d -> String.format("%srankdir=%s;\n", "", d.code)).orElse("") +
                    textAsLine(tab, toInsertBefore) +
                    links.stream()
                            .map(link -> String.format("%s%s -> %s\n", "", link.from, link.to))
                            .distinct()
                            .collect(Collectors.joining()) +
                    textAsLine("", toInsertAfter)
                ) +
                "}\n" +
                "----\n"
                ;
    }

    private String tabulateText(String tab, String text) {
        if (text.isEmpty()) return "";
        return tab + String.join("\n" + tab, text.split("\n")) + "\n";
    }

    private String textAsLine(String tab, String text) {
        if (text.isEmpty()) return "";
        return text + "\n";
    }


    public GraphvizGenerator addLink(String from, String to) {
        return addLink(new Link(from, to));
    }

    public GraphvizGenerator addLink(Link link) {
        links.add(link);
        return this;
    }


    public GraphvizGenerator rankDir(RankDir direction) {
        this.direction = Optional.of(direction);
        return this;
    }
}
