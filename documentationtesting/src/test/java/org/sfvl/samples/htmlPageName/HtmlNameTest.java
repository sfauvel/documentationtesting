package org.sfvl.samples.htmlPageName;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.HtmlPageExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.test_tools.OnlyRunProgrammatically;

import java.nio.file.Path;
import java.nio.file.Paths;

@NotIncludeToDoc
@OnlyRunProgrammatically
@ExtendWith(HtmlNameTest.HtmlPageHeaderExtension.class)
public class HtmlNameTest {
    static class HtmlPageHeaderExtension extends HtmlPageExtension {
        @Override
        public Path getFilePath(Class<?> clazz) {
            return new DocPath(clazz).page().folder().resolve("index.adoc");
        }
    }

    @RegisterExtension
    static final ApprovalsExtension doc = new SimpleApprovalsExtension();

    @Test
    public void test_A() {
        doc.write("In my *test*");
    }

}
