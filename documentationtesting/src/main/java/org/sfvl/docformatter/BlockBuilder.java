package org.sfvl.docformatter;


public interface BlockBuilder<T> {
    T title(String source_code);
    T content(String content);
    String build();
}

interface SourceCodeBuilder extends BlockBuilder<SourceCodeBuilder> {
    SourceCodeBuilder source(String s);
    SourceCodeBuilder indent(int i);
}
