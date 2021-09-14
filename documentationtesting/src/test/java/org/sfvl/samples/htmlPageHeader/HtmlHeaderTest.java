package org.sfvl.samples.htmlPageHeader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.HtmlPageExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.test_tools.OnlyRunProgrammatically;

@NotIncludeToDoc
@OnlyRunProgrammatically
@ExtendWith(HtmlHeaderTest.HtmlPageHeaderExtension.class)
public class HtmlHeaderTest {
    static class HtmlPageHeaderExtension extends HtmlPageExtension {
        @Override
        public String content(Class<?> clazz) {
            return String.join("\n",
                    ":toc: left",
                    ":nofooter:",
                    super.content(clazz));
        }
    }

    @RegisterExtension
    static final ApprovalsExtension doc = new SimpleApprovalsExtension();

    @Test
    public void test_A() {
        doc.write("In my *test*");
    }

}
