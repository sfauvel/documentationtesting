package org.sfvl.docformatter;

import java.util.List;

public interface Formatter {

    String standardOptions();

    String title(int index, String title);

    String description(String description);

    String paragraph(String... content);

    String paragraphSuite(String... paragraph);

    String tableOfContent();

    String tableOfContent(int level);

    String addDefinition(String key, String description);

    String listItem(String text);

    String listItems(String... texts);

    String listItemsWithTitle(String title, String... texts);

    String sourceCode(String source);

    String include(String filename);

    String include(String filename, int offset);

    String warning(String message);

    String section(String name, String message);

    String anchor(String id);

    String linkToAnchor(String id, String visibleText);

    String linkToPage(String address, String visibleText);

    String linkToPage(String address, String anchor, String visibleText);


    String table(List<List<? extends Object>> data);

    String tableWithHeader(List<List<? extends Object>> asList);

    String tableWithHeader(List<?> header, List<List<?>> data);

    String image(String filename);
    String image(String github_logo_path, String title);

    String sourceFragment(String s, String interestingCode);

    String blockId(String id);

    BlockBuilder blockBuilder(Block block);
    BlockBuilder blockBuilder(String delimiter);

    SourceCodeBuilder sourceCodeBuilder();

    SourceCodeBuilder sourceCodeBuilder(String language);

    String attribute(String attribute, String value);

    String bold(String text);
    String italic(String text);

    public static enum Block {
        LITERAL, CODE;
    }
}
