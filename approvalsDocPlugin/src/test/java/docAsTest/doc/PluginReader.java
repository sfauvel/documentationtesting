package docAsTest.doc;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PluginReader {


    public static class PluginAction {
        private String text;
        private String description;
        private String first_shortcut;
        private String second_shortcut;

        public PluginAction(String text, String description, String shortcut) {
            this.text = text;
            this.description = description;
            this.first_shortcut = shortcut;
        }

        public PluginAction(Node action) {
            this.text = getAttributeValue(action, "text");
            this.description = getAttributeValue(action, "description");
            final Optional<Node> nodeShortcut = getFirstChildByTagName(action, "keyboard-shortcut");
            this.first_shortcut = getAttributeValue(nodeShortcut, "first-keystroke");
            this.second_shortcut = getAttributeValue(nodeShortcut, "second-keystroke");
        }

        public String getText() {
            return text;
        }

        public String getDescription() {
            return description;
        }

        public String getFirst_shortcut() {
            return first_shortcut;
        }

        public String getSecond_shortcut() {
            return second_shortcut;
        }

        public String getFormattedShortcut() {
            return formatShortcut(getFirst_shortcut(), getSecond_shortcut());
        }

        @NotNull
        private String formatShortcut(String first_shortcut, String second_shortcut) {
            return Optional.of(first_shortcut).map(n -> String.format("%s+%s",
                            first_shortcut
                                    .replace("control", "Ctrl")
                                    .replace("alt", "Alt")
                                    .replace(" ", "+"),
                            second_shortcut
                    )
            ).orElse("");
        }


        private static String getAttributeValue(Optional<Node> node, String tag) {
            return node.map(Node::getAttributes)
                    .map(nodeMap -> nodeMap.getNamedItem(tag))
                    .map(Node::getTextContent)
                    .orElse("");
        }
        private static String getAttributeValue(Node node, String tag) {
            return getAttributeValue(Optional.ofNullable(node), tag);
        }

        private Optional<Node> getFirstChildByTagName(Node action, String tab) {
            final List<Node> nodes = getChildsByTagName(action, tab);
            return nodes.isEmpty()
                    ? Optional.empty()
                    : Optional.of(nodes.get(0));
        }

        private List<Node> getChildsByTagName(Node node, String tag) {
            final NodeList childNodes = node.getChildNodes();
            final List<Node> filteredChilds = new ArrayList<>();
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node childNode = childNodes.item(i);
                if (childNode.getNodeName().equals(tag)) {
                    filteredChilds.add(childNode);
                }
            }
            return filteredChilds;
        }

    }

    public List<PluginAction> getActions() throws ParserConfigurationException, IOException, SAXException {

        final Document pluginXml = getPluginXmlDocument();
        Element root = pluginXml.getDocumentElement();

        final List<Node> nodes = toList(root.getElementsByTagName("action"));

        return nodes.stream()
                .map(action -> new PluginAction(action))
                .collect(Collectors.toList());
    }

    private Document getPluginXmlDocument() throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        final Document parse = builder.parse(Paths.get("src", "main", "resources", "META-INF", "plugin.xml").toFile());
        return parse;
    }


    private List<Node> toList(NodeList nodeList) {
        final List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            nodes.add(nodeList.item(i));
        }
        return nodes;
    }


}
