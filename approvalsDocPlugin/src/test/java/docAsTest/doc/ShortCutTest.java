package docAsTest.doc;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.sfvl.docformatter.BlockBuilder;
import org.sfvl.docformatter.Formatter;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import tools.ApprovalsJUnit4;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RunWith(JUnit4.class)
public class ShortCutTest extends ApprovalsJUnit4 {

    final static AsciidocFormatter formatter = new AsciidocFormatter();

    /**
     * This plugin provide contextual menu to navigate between files related to a DocAsTest test.
     */
    @Test
    public void plugin_actions() throws ParserConfigurationException, IOException, SAXException {

        final Document pluginXml = getPluginXmlDocument();
        Element root = pluginXml.getDocumentElement();

        final List<Node> nodes = toList(root.getElementsByTagName("action"));
        final String actionTable = formatter.tableWithHeader(Arrays.asList("Text", "Description", "Shortcut"),
                nodes.stream()
                        .map(action -> {
                            final Optional<Node> nodeShortcut = getFirstChildByTagName(action, "keyboard-shortcut");
                            return Arrays.asList(
                                    getAttributeValue(action, "text"),
                                    getAttributeValue(action, "description"),
                                    "\n[%nowrap]\n" +
                                    formatter.blockBuilder(Formatter.Block.CODE)
                                            .content(formatShortcut(nodeShortcut))
                                            .build()
                            );
                        })
                        .collect(Collectors.toList())
        );

        write("[%autowidth, cols=\",,a\"]",
                actionTable);
    }

    private Document getPluginXmlDocument() throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        final Document parse = builder.parse(Paths.get("src", "main", "resources", "META-INF", "plugin.xml").toFile());
        return parse;
    }

    @NotNull
    private String formatShortcut(Optional<Node> nodeShortcut) {
        return nodeShortcut.map(n -> String.format("%s+%s",
                        getAttributeValue(n, "first-keystroke")
                                .replace("control", "Ctrl")
                                .replace("alt", "Alt")
                                .replace(" ", "+"),
                        getAttributeValue(n, "second-keystroke")
                )
        ).orElse("");
    }


    private List<Node> toList(NodeList nodeList) {
        final List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            nodes.add(nodeList.item(i));
        }
        return nodes;
    }

    private String getAttributeValue(Node node, String tab) {
        final NamedNodeMap attributes = node.getAttributes();
        return attributes.getNamedItem(tab).getTextContent();
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
