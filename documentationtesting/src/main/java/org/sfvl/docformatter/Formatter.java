package org.sfvl.docformatter;

import java.util.List;

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


}
