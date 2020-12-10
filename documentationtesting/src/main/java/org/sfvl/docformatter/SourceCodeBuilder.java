package org.sfvl.docformatter;

public interface SourceCodeBuilder {
    SourceCodeBuilder title(String source_code);

    SourceCodeBuilder language(String language);

    SourceCodeBuilder indent(int i);

    SourceCodeBuilder source(String s);

    String build();
}
