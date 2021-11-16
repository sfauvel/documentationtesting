package org.sfvl.howto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GraphvizGenerator {
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
    List<Link> links = new ArrayList<>();

    public String generate() {
        return generate("", "");
    }

    public String generate(String toInsertBefore, String toInsertAfter) {
        return "\n[graphviz]\n" +
                "----\n" +
                "digraph g {\n" +
                "    rankdir=LR;\n" +
                toInsertBefore +
                links.stream()
                        .map(link -> link.from + " -> " + link.to + "\n")
                        .distinct()
                        .collect(Collectors.joining())+
                toInsertAfter +
                "}\n" +
                "----\n"
                ;
    }


    public GraphvizGenerator addLink(String from, String to) {
        return addLink(new Link(from, to));
    }

    public GraphvizGenerator addLink(Link link) {
        links.add(link);
        return this;
    }
}
