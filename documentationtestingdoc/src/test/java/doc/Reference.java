package doc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.Formatter;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.utils.NoTitle;
import tools.CustomPageExtension;

@ExtendWith(CustomPageExtension.class)
public class Reference {

    @RegisterExtension
    public static ApprovalsExtension doc = new SimpleApprovalsExtension();
    Formatter formatter = new AsciidocFormatter();

    /**
     * In this page, you will find links to resources that help you to implement a documentation that can be used as a non regression test.
     */
    @Test
    @NoTitle
    public void runner() {

        doc.write(formatter.paragraph(
                //"[.subtitle]",
                //"Information-oriented",
                "",
                formatter.listItems(
                        "Documentation of the concept: link:{github-pages}[]",
                        "Library for Java: " + Links.JavaDocAsTest(),
                        "Library for Python and pytest: " + Links.PythonDocAsTest(),
                        "To get source and examples: link:{github-repo}[]",
                        "A plugin for IntelliJ is provided in {github-repo}/approvalsDocPlugin[approvalsDocPlugin] folder")
        ));
    }
}
