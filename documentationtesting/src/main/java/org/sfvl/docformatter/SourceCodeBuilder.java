package org.sfvl.docformatter;

public interface SourceCodeBuilder extends GenericBlockBuilder<SourceCodeBuilder> {
    SourceCodeBuilder source(String s);

    SourceCodeBuilder indent(int i);
}
