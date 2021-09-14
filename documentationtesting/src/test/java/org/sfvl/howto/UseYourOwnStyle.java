package org.sfvl.howto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;

/**
 * To have specific style in some chapter, you can use HTML style directly in your Asciidoc files.
 *
 * You need to encapsulate it between \\++++ and <style> balise.
 */
public class UseYourOwnStyle {

    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    private final AsciidocFormatter formatter = new AsciidocFormatter();

    /**
     * You can customize style adding a css style directly in your file.
     * You need to put an html style tag (`<style></style>`) enclosed in `\++++++`
     * to add it without processed content and send it directly to the output.
     */
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

        final String text = String.join("\n",
                "This text should be black.",
                "",
                "[withColor]#This text shoud be blue.#",
                "",
                "This text should be black.",
                "",
                "[withColor]",
                "--",
                "This section formed by several lines in asciidoc",
                "should be blue.",
                "--",
                "This text should be black."
        );

        final String content = String.format("%s\n%s", style, text);

        doc.write(formatter.sourceCodeBuilder("html")
                .title("Asciidoc content")
                .source(content)
                .build(),
                "", "");

        doc.write(formatter.blockBuilder("====")
                .title("Content rendering")
                .content(content)
                .build(),
                "", "");
    }
}
