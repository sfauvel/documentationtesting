package org.sfvl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.development.Development;
import org.sfvl.docformatter.AsciidocFormatterTest;
import org.sfvl.docformatter.Formatter;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.DocTestingDocumentation;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.ApprovalsExtensionTest;
import org.sfvl.doctesting.junitextension.HtmlPageExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.NoTitle;
import org.sfvl.howto.HowTo;
import org.sfvl.howto.KnownIssues;
import org.sfvl.howto.Tutorial;

import java.nio.file.Path;
import java.nio.file.Paths;

@DisplayName(value = "Documentation Testing Library")
@ExtendWith(DocumentationTestingDocumentation.HtmlPage.class)
public class DocumentationTestingDocumentation {

    static class HtmlPage extends HtmlPageExtension {
        @Override
        public String content(Class<?> clazz) {
            return String.join("\n",
                    doc.getDocWriter().defineDocPath(Paths.get(".")),
                    ":nofooter:",
                    super.content(clazz));
        }

        @Override
        public Path getFilePath(Class<?> clazz) {
            return Config.DOC_PATH.resolve("index.adoc");
        }
    }

    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    private final Formatter formatter = new AsciidocFormatter();

    @Test
    @NoTitle
    public void documentationTesting() {
        doc.write(getContent());
    }

    public String getContent() {
        final DocPath docPath = new DocPath(Paths.get(""), "indexContent");
        final Path from = docPath.resource().from(new DocPath(this.getClass()).approved());

        return String.join("\n",
                formatter.attribute("TUTORIAL_HTML", htmlPath(Tutorial.class)),
                formatter.attribute("HOW_TO_HTML", htmlPath(HowTo.class)),
                formatter.attribute("APPROVAL_EXTENSION_HTML", htmlPath(ApprovalsExtensionTest.class)),
                formatter.attribute("ASCIIDOC_FORMATTER_HTML", htmlPath(AsciidocFormatterTest.class)),
                formatter.attribute("DOC_TESTING_DOCUMENTATION_HTML", htmlPath(DocTestingDocumentation.class)),
                formatter.attribute("DEVELOPMENT", htmlPath(Development.class)),
                formatter.attribute("KNOWN_ISSUES_HTML", htmlPath(KnownIssues.class)),
                String.format("include::%s[leveloffset=+1]", DocPath.toAsciiDoc(from))
        );
    }

    private String htmlPath(Class<?> clazz) {
        return DocPath.toAsciiDoc(new DocPath(clazz).html().path());
    }

}
