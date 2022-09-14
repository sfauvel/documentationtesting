package docAsTest.doc;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.sfvl.docformatter.Formatter;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.xml.sax.SAXException;
import tools.ApprovalsJUnit4;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@DisplayName("Shortcuts")
@RunWith(JUnit4.class)
public class ShortCutTest extends ApprovalsJUnit4 {

    final static AsciidocFormatter formatter = new AsciidocFormatter();

    /**
     * This plugin provide contextual menu to navigate between files related to a DocAsTest test.
     */
    @Test
    public void plugin_actions() throws ParserConfigurationException, IOException, SAXException {
        final PluginReader pluginReader = new PluginReader();
        final List<PluginReader.PluginAction> actions = pluginReader.getActions();

        final String actions_table = formatter.tableWithHeader(Arrays.asList("Text", "Description", "Shortcut"),
                actions.stream()
                        .map(action -> Arrays.asList(
                                action.getText(),
                                action.getDescription(),
                                "\n[%nowrap]\n" +
                                        formatter.blockBuilder(Formatter.Block.CODE)
                                                .content(action.getFormattedShortcut())
                                                .build()

                        ))
                        .collect(Collectors.toList()));

        write("[%autowidth, cols=\",,a\"]",
                actions_table);
    }

}
