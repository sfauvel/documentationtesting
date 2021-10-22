package org.sfvl.samples;


import org.sfvl.docformatter.BlockBuilder;
import org.sfvl.docformatter.SourceCodeBuilder;

import java.util.List;

public class MyFormatter implements org.sfvl.docformatter.Formatter  {
    @Override
    public String standardOptions() {
        return null;
    }

    @Override
    public String title(int index, String title) {
        return null;
    }

    @Override
    public String description(String description) {
        return null;
    }

    @Override
    public String paragraph(String... content) {
        return null;
    }

    @Override
    public String paragraphSuite(String... paragraph) {
        return null;
    }

    @Override
    public String tableOfContent() {
        return null;
    }

    @Override
    public String tableOfContent(int level) {
        return null;
    }

    @Override
    public String addDefinition(String key, String description) {
        return null;
    }

    @Override
    public String listItem(String text) {
        return null;
    }

    @Override
    public String listItems(String... texts) {
        return null;
    }

    @Override
    public String listItemsWithTitle(String title, String... texts) {
        return null;
    }

    @Override
    public String sourceCode(String source) {
        return null;
    }

    @Override
    public String include(String filename) {
        return null;
    }

    @Override
    public String include(String filename, int offset) {
        return null;
    }

    @Override
    public String warning(String message) {
        return null;
    }

    @Override
    public String section(String name, String message) {
        return null;
    }

    @Override
    public String link(String id) {
        return null;
    }

    @Override
    public String anchorLink(String id, String visibleText) {
        return null;
    }

    @Override
    public String table(List<List<?>> data) {
        return null;
    }

    @Override
    public String tableWithHeader(List<List<?>> asList) {
        return null;
    }

    @Override
    public String image(String filename) {
        return null;
    }

    @Override
    public String sourceFragment(String s, String interestingCode) {
        return null;
    }

    @Override
    public BlockBuilder blockBuilder(Block block) {
        return null;
    }

    @Override
    public BlockBuilder blockBuilder(String delimiter) {
        return null;
    }

    @Override
    public SourceCodeBuilder sourceCodeBuilder() {
        return null;
    }

    @Override
    public SourceCodeBuilder sourceCodeBuilder(String language) {
        return null;
    }

    @Override
    public String attribute(String attribute, String value) {
        return null;
    }
}
