package org.sfvl.test_tools;

import org.sfvl.doctesting.junitextension.HtmlPageExtension;
import org.sfvl.doctesting.utils.DocPath;

public class IntermediateHtmlPage extends HtmlPageExtension {
    @Override
    public String content(Class<?> clazz) {
        return String.join("\n",
                ":toc: left",
                ":nofooter:",
                ":stem:",
                ":source-highlighter: rouge",
                ":toclevels: 4",
                "",
                String.format("include::%s[]", new DocPath(clazz).approved().fullname()));
    }
}
