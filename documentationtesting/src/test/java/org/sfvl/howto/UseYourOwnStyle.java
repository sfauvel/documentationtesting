package org.sfvl.howto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.doctesting.utils.DocWriter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;

/**
 * To have specific style in some chapter, you can use HTML style directly in your Asciidoc files.
 *
 * You need to encapsulate it between \\++++ and <style> balise.
 */
public class UseYourOwnStyle {

    private static final DocWriter doc = new DocWriter();
    @RegisterExtension
    static ApprovalsExtension extension = new ApprovalsExtension(doc);

    private final AsciidocFormatter formatter = new AsciidocFormatter();

    @Test
    public void define_a_custom_style() {
        final String style = String.join("\n",
                "++++",
                "<style>",
                ".withColor {",
                "   color: blue;",
                "}",
                "</style>",
                "++++");

        final String text = String.join("\n\n",
                "This text should be black.",
                "[withColor]#This text shoud be blue.#",
                "This text is not anymore blue."
        );

        final String content = String.format("%s\n\n%s", style, text);

        doc.write(formatter.sourceCodeBuilder("html")
                .title("Asciidoc content")
                .source(content)
                .build());

        doc.write("\n\n");

        doc.write(formatter.blockBuilder("====")
                .title("Content rendering")
                .content(content)
                .build());


    }
}
