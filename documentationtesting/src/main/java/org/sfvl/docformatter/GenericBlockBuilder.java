package org.sfvl.docformatter;


public interface GenericBlockBuilder<T> {
    T title(String source_code);
    T content(String content);
    String build();
}

