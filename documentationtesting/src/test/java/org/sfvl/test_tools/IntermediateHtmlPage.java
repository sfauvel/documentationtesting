package org.sfvl.test_tools;

import org.sfvl.doctesting.junitextension.HtmlPageExtension;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.DocWriter;

public class IntermediateHtmlPage extends HtmlPageExtension {
    @Override
    public String content(Class<?> clazz) {

        return String.join("\n",
                new DocWriter().defineDocPath(clazz),
                ":stylesdir: {ROOT_PATH}/../resources/styles",
                ":toc: left",
                ":nofooter:",
                ":stem:",
                ":source-highlighter: rouge",
                ":rouge-style: thankful_eyes",
                ":toclevels: 4",
                "",
                String.format("include::%s[]", new DocPath(clazz).approved().fullname()),
                "",
                "[subs=attributes+]",
                "++++",
                "include::{stylesdir}/logo.adoc[]",
                "<style>",
                "include::{stylesdir}/documentation.css[]",
                "</style>",
                "<script type=\"text/javascript\">",
                "include::{stylesdir}/documentation.js[]",
                "</script>",
                "++++"
        );
    }
}
