package org.sfvl.samples.htmlPageConstructor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.HtmlPageExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.test_tools.OnlyRunProgrammatically;

import java.nio.file.Path;

@NotIncludeToDoc
@OnlyRunProgrammatically
public class HtmlNameConstructorTest {
    @RegisterExtension
    static final ApprovalsExtension doc = new SimpleApprovalsExtension();

    static Path path = new DocPath(HtmlNameConstructorTest.class).packagePath();
    @RegisterExtension
    static final HtmlPageExtension page = new HtmlPageExtension(path.resolve("index").toString());

    @Test
    public void test_A() {
        doc.write("In my *test*");
    }

}
